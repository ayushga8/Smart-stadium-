package com.smartstadium.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

class JwtServiceTest {

    private static final String SECRET = "TestSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmTesting!!!";
    private static final String DIFFERENT_SECRET = "DifferentSecretKeyThatIsAlso256BitsLongForHS256AlgorithmTests!!";
    private static final long ACCESS_EXPIRY_MS = 900_000;    // 15 min
    private static final long REFRESH_EXPIRY_MS = 604_800_000; // 7 days

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
    }

    @Nested
    @DisplayName("Access token generation")
    class AccessTokenGeneration {

        @Test
        @DisplayName("should generate a non-null access token")
        void shouldGenerateNonNullAccessToken() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should contain the correct email as subject")
        void shouldContainCorrectSubject() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.getSubject()).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("should contain type=access claim")
        void shouldContainAccessTypeClaim() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.get("type", String.class)).isEqualTo("access");
        }

        @Test
        @DisplayName("should set expiration approximately 15 minutes in the future")
        void shouldHaveCorrectExpiry() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            Claims claims = jwtService.extractClaims(token);
            long diff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
            assertThat(diff).isEqualTo(ACCESS_EXPIRY_MS);
        }

        @Test
        @DisplayName("should generate different tokens for different emails")
        void shouldGenerateDifferentTokensForDifferentEmails() {
            String token1 = jwtService.generateAccessToken("a@test.com", "USER");
            String token2 = jwtService.generateAccessToken("b@test.com", "USER");
            assertThat(token1).isNotEqualTo(token2);
        }

        @Test
        @DisplayName("should generate different tokens on subsequent calls for same email")
        void shouldGenerateDifferentTokensOnSubsequentCalls() throws InterruptedException {
            String token1 = jwtService.generateAccessToken("user@test.com", "USER");
            Thread.sleep(1100); // JWT iat is in seconds — need >1s gap
            String token2 = jwtService.generateAccessToken("user@test.com", "USER");
            assertThat(token1).isNotEqualTo(token2);
        }
    }

    @Nested
    @DisplayName("Refresh token generation")
    class RefreshTokenGeneration {

        @Test
        @DisplayName("should generate a non-null refresh token")
        void shouldGenerateNonNullRefreshToken() {
            String token = jwtService.generateRefreshToken("user@test.com");
            assertThat(token).isNotNull().isNotBlank();
        }

        @Test
        @DisplayName("should contain type=refresh claim")
        void shouldContainRefreshTypeClaim() {
            String token = jwtService.generateRefreshToken("user@test.com");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.get("type", String.class)).isEqualTo("refresh");
        }

        @Test
        @DisplayName("should set expiration approximately 7 days in the future")
        void shouldHaveCorrectExpiry() {
            String token = jwtService.generateRefreshToken("user@test.com");
            Claims claims = jwtService.extractClaims(token);
            long diff = claims.getExpiration().getTime() - claims.getIssuedAt().getTime();
            assertThat(diff).isEqualTo(REFRESH_EXPIRY_MS);
        }

        @Test
        @DisplayName("access and refresh tokens should have different expiry durations")
        void accessAndRefreshShouldHaveDifferentExpiry() {
            String access = jwtService.generateAccessToken("user@test.com", "USER");
            String refresh = jwtService.generateRefreshToken("user@test.com");

            Claims accessClaims = jwtService.extractClaims(access);
            Claims refreshClaims = jwtService.extractClaims(refresh);

            long accessDuration = accessClaims.getExpiration().getTime() - accessClaims.getIssuedAt().getTime();
            long refreshDuration = refreshClaims.getExpiration().getTime() - refreshClaims.getIssuedAt().getTime();

            assertThat(refreshDuration).isGreaterThan(accessDuration);
        }
    }

    @Nested
    @DisplayName("Token validation")
    class TokenValidation {

        @Test
        @DisplayName("should validate a correctly signed token")
        void shouldValidateCorrectToken() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            assertThat(jwtService.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("should reject an expired token")
        void shouldRejectExpiredToken() {
            JwtService shortLivedService = new JwtService(SECRET, 0, 0);
            String token = shortLivedService.generateAccessToken("user@test.com", "USER");
            // token is already expired (0ms expiry)
            assertThat(shortLivedService.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("should reject a tampered token")
        void shouldRejectTamperedToken() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            // Flip a character in the signature portion
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";
            assertThat(jwtService.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("should reject a malformed token string")
        void shouldRejectMalformedToken() {
            assertThat(jwtService.validateToken("not.a.valid.jwt")).isFalse();
        }

        @Test
        @DisplayName("should reject an empty token")
        void shouldRejectEmptyToken() {
            assertThat(jwtService.validateToken("")).isFalse();
        }

        @Test
        @DisplayName("should reject a null token")
        void shouldRejectNullToken() {
            assertThat(jwtService.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("should reject a token signed with a different key")
        void shouldRejectTokenSignedWithDifferentKey() {
            JwtService otherService = new JwtService(DIFFERENT_SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);
            String token = otherService.generateAccessToken("user@test.com", "USER");
            assertThat(jwtService.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("should reject a token with alg=none (unsigned)")
        void shouldRejectAlgNoneToken() {
            // Manually craft a JWT with alg=none
            String header = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());
            String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"sub\":\"user@test.com\",\"exp\":9999999999}".getBytes());
            String unsignedToken = header + "." + payload + ".";

            assertThat(jwtService.validateToken(unsignedToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("Claim extraction")
    class ClaimExtraction {

        @Test
        @DisplayName("should extract email from access token")
        void shouldExtractEmailFromAccessToken() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("should extract email from refresh token")
        void shouldExtractEmailFromRefreshToken() {
            String token = jwtService.generateRefreshToken("user@test.com");
            assertThat(jwtService.extractEmail(token)).isEqualTo("user@test.com");
        }

        @Test
        @DisplayName("should throw when extracting claims from expired token")
        void shouldThrowForExpiredToken() {
            JwtService shortLived = new JwtService(SECRET, 0, 0);
            String token = shortLived.generateAccessToken("user@test.com", "USER");
            assertThatThrownBy(() -> jwtService.extractClaims(token))
                    .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
        }
    }
}
