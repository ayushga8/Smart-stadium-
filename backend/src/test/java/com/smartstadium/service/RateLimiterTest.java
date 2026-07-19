package com.smartstadium.service;

import com.smartstadium.exception.RateLimitExceededException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RateLimiterTest {

    @Mock
    private JavaMailSender mailSender;

    private OtpService otpService;

    @BeforeEach
    void setUp() {
        otpService = new OtpService(mailSender);
    }

    @Test
    @DisplayName("should allow first OTP request")
    void shouldAllowFirstRequest() {
        assertThatCode(() -> otpService.generateAndSend("rate1@test.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should allow second OTP request")
    void shouldAllowSecondRequest() {
        otpService.generateAndSend("rate2@test.com");
        assertThatCode(() -> otpService.generateAndSend("rate2@test.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should allow third OTP request (at limit)")
    void shouldAllowThirdRequest() {
        otpService.generateAndSend("rate3@test.com");
        otpService.generateAndSend("rate3@test.com");
        assertThatCode(() -> otpService.generateAndSend("rate3@test.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should block fourth OTP request (over limit)")
    void shouldBlockFourthRequest() {
        otpService.generateAndSend("rate4@test.com");
        otpService.generateAndSend("rate4@test.com");
        otpService.generateAndSend("rate4@test.com");
        assertThatThrownBy(() -> otpService.generateAndSend("rate4@test.com"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("should maintain separate rate limit buckets per email")
    void shouldHaveSeparateBuckets() {
        // Exhaust one email's limit
        otpService.generateAndSend("limited@test.com");
        otpService.generateAndSend("limited@test.com");
        otpService.generateAndSend("limited@test.com");

        // Another email should be independent
        assertThatCode(() -> otpService.generateAndSend("fresh@test.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("should treat different case emails as same bucket")
    void shouldTreatDifferentCaseAsSameBucket() {
        otpService.generateAndSend("CASE@TEST.COM");
        otpService.generateAndSend("case@test.com");
        otpService.generateAndSend("Case@Test.Com");
        assertThatThrownBy(() -> otpService.generateAndSend("case@test.com"))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    @DisplayName("should report correct error message when rate limited")
    void shouldReportCorrectMessage() {
        otpService.generateAndSend("msg@test.com");
        otpService.generateAndSend("msg@test.com");
        otpService.generateAndSend("msg@test.com");
        assertThatThrownBy(() -> otpService.generateAndSend("msg@test.com"))
                .hasMessageContaining("Too many OTP requests");
    }

    @Test
    @DisplayName("rate limit state should persist across different OTP operations")
    void rateLimitShouldPersist() {
        otpService.generateAndSend("persist@test.com");
        // Verify and consume the OTP
        String otp = otpService.getOtpStore().get("persist@test.com").otp;
        otpService.verify("persist@test.com", otp);

        // Still consumed 2 more tokens of rate limit
        otpService.generateAndSend("persist@test.com");
        otpService.generateAndSend("persist@test.com");

        assertThatThrownBy(() -> otpService.generateAndSend("persist@test.com"))
                .isInstanceOf(RateLimitExceededException.class);
    }
}
