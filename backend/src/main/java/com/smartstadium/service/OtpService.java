package com.smartstadium.service;

import com.smartstadium.exception.InvalidOtpException;
import com.smartstadium.exception.OtpExpiredException;
import com.smartstadium.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OtpService {

    private final JavaMailSender mailSender;

    // OTP storage: email -> OtpEntry
    private final ConcurrentHashMap<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    // Rate limiter: email -> Bucket
    private final ConcurrentHashMap<String, Bucket> rateLimiters = new ConcurrentHashMap<>();

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_MINUTES = 5;
    private static final SecureRandom RANDOM = new SecureRandom();

    public OtpService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void generateAndSend(String email) {
        checkRateLimit(email);

        String otp = generateOtp();
        Instant expiry = Instant.now().plusSeconds(OTP_TTL_MINUTES * 60);
        otpStore.put(email.toLowerCase(), new OtpEntry(otp, expiry, false));

        sendOtpEmail(email, otp);
        log.info("OTP generated for email: {}", email.replaceAll("(.{2}).+(@.+)", "$1***$2"));
    }

    public boolean verify(String email, String otp) {
        String key = email.toLowerCase();
        OtpEntry entry = otpStore.get(key);

        if (entry == null) {
            throw new InvalidOtpException("No pending OTP for this email");
        }
        if (entry.used) {
            otpStore.remove(key);
            throw new InvalidOtpException("OTP has already been used");
        }
        if (Instant.now().isAfter(entry.expiry)) {
            otpStore.remove(key);
            throw new OtpExpiredException("OTP has expired");
        }
        if (!entry.otp.equals(otp)) {
            throw new InvalidOtpException("Invalid OTP code");
        }

        entry.used = true;
        otpStore.remove(key);
        return true;
    }

    @Async
    protected void sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Smart Stadium - Your Login Code");
            message.setText("Your verification code is: " + otp + "\n\nThis code expires in 5 minutes.\n\nFIFA World Cup 2026 - Smart Stadium");
            mailSender.send(message);
            log.info("OTP email sent successfully");
        } catch (Exception e) {
            // Don't orphan the OTP — remove it so user can retry
            otpStore.remove(email.toLowerCase());
            log.error("Failed to send OTP email", e);
        }
    }

    private void checkRateLimit(String email) {
        Bucket bucket = rateLimiters.computeIfAbsent(email.toLowerCase(), k ->
                Bucket.builder()
                        .addLimit(Bandwidth.builder()
                                .capacity(3)
                                .refillGreedy(3, Duration.ofMinutes(10))
                                .build())
                        .build());

        if (!bucket.tryConsume(1)) {
            throw new RateLimitExceededException("Too many OTP requests. Please wait before trying again.");
        }
    }

    String generateOtp() {
        StringBuilder sb = new StringBuilder(OTP_LENGTH);
        for (int i = 0; i < OTP_LENGTH; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }

    @Scheduled(fixedRate = 60000) // every minute
    public void cleanupExpiredOtps() {
        Instant now = Instant.now();
        otpStore.entrySet().removeIf(entry -> now.isAfter(entry.getValue().expiry));
    }

    // Visible for testing
    public ConcurrentHashMap<String, OtpEntry> getOtpStore() {
        return otpStore;
    }

    public ConcurrentHashMap<String, Bucket> getRateLimiters() {
        return rateLimiters;
    }

    public static class OtpEntry {
        public final String otp;
        public final Instant expiry;
        public volatile boolean used;

        public OtpEntry(String otp, Instant expiry, boolean used) {
            this.otp = otp;
            this.expiry = expiry;
            this.used = used;
        }
    }
}
