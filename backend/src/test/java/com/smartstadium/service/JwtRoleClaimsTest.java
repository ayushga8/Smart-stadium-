package com.smartstadium.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JWT role claims")
class JwtRoleClaimsTest {

    private static final String SECRET =
            "SmartStadium2026SecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmTesting!!!";
    private static final long ACCESS_EXPIRY_MS = 900_000;
    private static final long REFRESH_EXPIRY_MS = 604_800_000;

    private final JwtService jwtService = new JwtService(SECRET, ACCESS_EXPIRY_MS, REFRESH_EXPIRY_MS);

    @Nested
    @DisplayName("Role in access token")
    class RoleInToken {

        @Test
        @DisplayName("should include role claim in access token")
        void tokenContainsRoleClaim() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.get("role", String.class)).isNotNull();
        }

        @Test
        @DisplayName("should have USER role in claim for regular user")
        void userRoleInClaim() {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.get("role", String.class)).isEqualTo("USER");
        }

        @Test
        @DisplayName("should have ADMIN role in claim for admin")
        void adminRoleInClaim() {
            String token = jwtService.generateAccessToken("admin@test.com", "ADMIN");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("should have VOLUNTEER role in claim for volunteer")
        void volunteerRoleInClaim() {
            String token = jwtService.generateAccessToken("vol@test.com", "VOLUNTEER");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.get("role", String.class)).isEqualTo("VOLUNTEER");
        }

        @Test
        @DisplayName("should preserve both email and role in same token")
        void bothEmailAndRolePresent() {
            String token = jwtService.generateAccessToken("admin@test.com", "ADMIN");
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.getSubject()).isEqualTo("admin@test.com");
            assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
            assertThat(claims.get("type", String.class)).isEqualTo("access");
        }

        @Test
        @DisplayName("tokens with different roles for same email should differ")
        void differentRolesProduceDifferentTokens() {
            String userToken = jwtService.generateAccessToken("test@test.com", "USER");
            String adminToken = jwtService.generateAccessToken("test@test.com", "ADMIN");
            assertThat(userToken).isNotEqualTo(adminToken);
        }

        @Test
        @DisplayName("role claim should be extractable from valid token")
        void roleExtractableFromValidToken() {
            String token = jwtService.generateAccessToken("test@test.com", "VOLUNTEER");

            // Validate and extract
            assertThat(jwtService.validateToken(token)).isTrue();
            Claims claims = jwtService.extractClaims(token);
            assertThat(claims.get("role")).isEqualTo("VOLUNTEER");
        }
    }

    @Nested
    @DisplayName("Role with token validation")
    class RoleWithValidation {

        @Test
        @DisplayName("expired token with role should fail validation")
        void expiredTokenWithRoleFails() {
            JwtService shortLived = new JwtService(SECRET, 0, 0);
            String token = shortLived.generateAccessToken("test@test.com", "ADMIN");
            assertThat(shortLived.validateToken(token)).isFalse();
        }

        @Test
        @DisplayName("tampered token should not expose role")
        void tamperedTokenCannotExtractRole() {
            String token = jwtService.generateAccessToken("test@test.com", "ADMIN");
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";

            assertThat(jwtService.validateToken(tampered)).isFalse();
        }

        @Test
        @DisplayName("role does not affect token validation")
        void allRolesPassValidation() {
            for (String role : new String[]{"USER", "VOLUNTEER", "ADMIN"}) {
                String token = jwtService.generateAccessToken("test@test.com", role);
                assertThat(jwtService.validateToken(token))
                        .as("Token with role %s should be valid", role)
                        .isTrue();
            }
        }
    }
}
