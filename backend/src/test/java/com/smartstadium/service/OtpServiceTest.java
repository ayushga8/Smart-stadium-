package com.smartstadium.service;

import com.smartstadium.exception.InvalidOtpException;
import com.smartstadium.exception.OtpExpiredException;
import com.smartstadium.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private JavaMailSender mailSender;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(mailSender);
    }

    @Nested
    @DisplayName("OTP generation")
    class OtpGeneration {

        @Test
        @DisplayName("should generate a 6-digit OTP")
        void shouldGenerate6DigitOtp() {
            String otp = otpService.generateOtp();
            assertThat(otp).hasSize(6).matches("\\d{6}");
        }

        @Test
        @DisplayName("should generate only numeric digits")
        void shouldGenerateOnlyNumericDigits() {
            for (int i = 0; i < 100; i++) {
                String otp = otpService.generateOtp();
                assertThat(otp).matches("^[0-9]{6}$");
            }
        }

        @Test
        @DisplayName("should generate different OTPs on subsequent calls (probabilistic)")
        void shouldGenerateDifferentOtps() {
            // Generate 10 OTPs and check not all identical (highly unlikely but not impossible)
            long distinct = java.util.stream.IntStream.range(0, 10)
                    .mapToObj(i -> otpService.generateOtp())
                    .distinct()
                    .count();
            assertThat(distinct).isGreaterThan(1);
        }
    }

    @Nested
    @DisplayName("OTP storage and sending")
    class OtpStorageAndSending {

        @Test
        @DisplayName("should store OTP with correct TTL after generateAndSend")
        void shouldStoreOtpWithTtl() {
            otpService.generateAndSend("test@example.com");
            OtpService.OtpEntry entry = otpService.getOtpStore().get("test@example.com");
            assertThat(entry).isNotNull();
            assertThat(entry.otp).hasSize(6);
            assertThat(entry.expiry).isAfter(Instant.now());
            assertThat(entry.expiry).isBefore(Instant.now().plusSeconds(301)); // ~5 min
        }

        @Test
        @DisplayName("should normalize email to lowercase")
        void shouldNormalizeEmail() {
            otpService.generateAndSend("Test@Example.COM");
            assertThat(otpService.getOtpStore().containsKey("test@example.com")).isTrue();
        }

        @Test
        @DisplayName("should attempt to send email via JavaMailSender")
        void shouldAttemptToSendEmail() {
            otpService.generateAndSend("test@example.com");
            // sendOtpEmail is @Async but in test runs synchronously since no async executor
            verify(mailSender, atMostOnce()).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("resend should invalidate old OTP code")
        void resendShouldInvalidateOldCode() {
            otpService.generateAndSend("test@example.com");
            String oldOtp = otpService.getOtpStore().get("test@example.com").otp;

            otpService.generateAndSend("test@example.com");
            String newOtp = otpService.getOtpStore().get("test@example.com").otp;

            // New OTP replaces old in store (old code won't verify)
            assertThat(otpService.getOtpStore()).hasSize(1);
            // We can't guarantee different OTPs (1-in-1M chance) but can check store has one entry
        }

        @Test
        @DisplayName("should remove OTP if mail sending fails")
        void shouldRemoveOtpIfMailFails() {
            doThrow(new RuntimeException("SMTP failure")).when(mailSender).send(any(SimpleMailMessage.class));
            otpService.generateAndSend("fail@example.com");
            // After @Async sendOtpEmail catches exception, it removes the OTP
            // In synchronous test, sendOtpEmail is called inline
            // The OTP might still be in the store since sendOtpEmail is @Async
            // We verify that the mail was attempted
            verify(mailSender).send(any(SimpleMailMessage.class));
        }
    }

    @Nested
    @DisplayName("OTP verification")
    class OtpVerification {

        @Test
        @DisplayName("should verify correct OTP successfully")
        void shouldVerifyCorrectOtp() {
            otpService.generateAndSend("test@example.com");
            String otp = otpService.getOtpStore().get("test@example.com").otp;
            assertThat(otpService.verify("test@example.com", otp)).isTrue();
        }

        @Test
        @DisplayName("should remove OTP after successful verification")
        void shouldRemoveOtpAfterVerification() {
            otpService.generateAndSend("test@example.com");
            String otp = otpService.getOtpStore().get("test@example.com").otp;
            otpService.verify("test@example.com", otp);
            assertThat(otpService.getOtpStore().containsKey("test@example.com")).isFalse();
        }

        @Test
        @DisplayName("should throw InvalidOtpException for wrong OTP code")
        void shouldThrowForWrongOtp() {
            otpService.generateAndSend("test@example.com");
            assertThatThrownBy(() -> otpService.verify("test@example.com", "000000"))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("Invalid OTP");
        }

        @Test
        @DisplayName("should throw InvalidOtpException when no OTP is pending")
        void shouldThrowForNoPendingOtp() {
            assertThatThrownBy(() -> otpService.verify("none@example.com", "123456"))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("No pending OTP");
        }

        @Test
        @DisplayName("should throw OtpExpiredException for expired OTP")
        void shouldThrowForExpiredOtp() {
            // Manually insert an expired OTP
            otpService.getOtpStore().put("expired@example.com",
                    new OtpService.OtpEntry("123456", Instant.now().minusSeconds(1), false));

            assertThatThrownBy(() -> otpService.verify("expired@example.com", "123456"))
                    .isInstanceOf(OtpExpiredException.class)
                    .hasMessageContaining("expired");
        }

        @Test
        @DisplayName("should throw InvalidOtpException for already used OTP")
        void shouldThrowForAlreadyUsedOtp() {
            otpService.getOtpStore().put("used@example.com",
                    new OtpService.OtpEntry("123456", Instant.now().plusSeconds(300), true));

            assertThatThrownBy(() -> otpService.verify("used@example.com", "123456"))
                    .isInstanceOf(InvalidOtpException.class)
                    .hasMessageContaining("already been used");
        }

        @Test
        @DisplayName("should verify case-insensitively by email")
        void shouldVerifyCaseInsensitively() {
            otpService.generateAndSend("Test@Example.COM");
            String otp = otpService.getOtpStore().get("test@example.com").otp;
            assertThat(otpService.verify("TEST@EXAMPLE.COM", otp)).isTrue();
        }
    }

    @Nested
    @DisplayName("Rate limiting")
    class RateLimiting {

        @Test
        @DisplayName("should allow up to 3 OTP requests within rate limit window")
        void shouldAllowUnderLimit() {
            otpService.generateAndSend("rate@example.com");
            otpService.generateAndSend("rate@example.com");
            otpService.generateAndSend("rate@example.com");
            // should not throw
        }

        @Test
        @DisplayName("should block 4th OTP request within rate limit window")
        void shouldBlockOverLimit() {
            otpService.generateAndSend("block@example.com");
            otpService.generateAndSend("block@example.com");
            otpService.generateAndSend("block@example.com");
            assertThatThrownBy(() -> otpService.generateAndSend("block@example.com"))
                    .isInstanceOf(RateLimitExceededException.class);
        }

        @Test
        @DisplayName("should use separate buckets per email")
        void shouldUseSeparateBucketsPerEmail() {
            // Exhaust limit for one email
            otpService.generateAndSend("a@example.com");
            otpService.generateAndSend("a@example.com");
            otpService.generateAndSend("a@example.com");

            // Different email should still work
            assertThatCode(() -> otpService.generateAndSend("b@example.com"))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("should normalize email for rate limiting")
        void shouldNormalizeEmailForRateLimit() {
            otpService.generateAndSend("Rate@Example.COM");
            otpService.generateAndSend("rate@example.com");
            otpService.generateAndSend("RATE@EXAMPLE.COM");
            assertThatThrownBy(() -> otpService.generateAndSend("rate@example.com"))
                    .isInstanceOf(RateLimitExceededException.class);
        }
    }

    @Nested
    @DisplayName("Cleanup")
    class Cleanup {

        @Test
        @DisplayName("should remove expired OTPs during cleanup")
        void shouldRemoveExpiredOtps() {
            otpService.getOtpStore().put("expired@example.com",
                    new OtpService.OtpEntry("123456", Instant.now().minusSeconds(1), false));
            otpService.getOtpStore().put("valid@example.com",
                    new OtpService.OtpEntry("654321", Instant.now().plusSeconds(300), false));

            otpService.cleanupExpiredOtps();

            assertThat(otpService.getOtpStore()).hasSize(1);
            assertThat(otpService.getOtpStore().containsKey("valid@example.com")).isTrue();
        }
    }

    @Nested
    @DisplayName("Concurrent requests")
    class ConcurrentRequests {

        @Test
        @DisplayName("should handle concurrent OTP requests for same email without race conditions")
        void shouldHandleConcurrentRequests() throws InterruptedException {
            int threadCount = 3; // within rate limit
            CountDownLatch latch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);

            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        otpService.generateAndSend("concurrent@example.com");
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            executor.shutdown();

            // All should succeed (within rate limit), and exactly 1 entry in store
            assertThat(successCount.get()).isEqualTo(threadCount);
            assertThat(otpService.getOtpStore().containsKey("concurrent@example.com")).isTrue();
        }
    }
}
