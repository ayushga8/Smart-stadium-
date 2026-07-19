package com.smartstadium.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstadium.dto.OtpRequestDto;
import com.smartstadium.dto.OtpVerifyDto;
import com.smartstadium.repository.UserRepository;
import com.smartstadium.service.JwtService;
import com.smartstadium.service.OtpService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private OtpService otpService;
    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        otpService.getOtpStore().clear();
        otpService.getRateLimiters().clear();
    }

    @Nested
    @DisplayName("SQL injection protection")
    class SqlInjectionProtection {

        @Test
        @DisplayName("should reject SQL injection in email field via validation")
        void shouldRejectSqlInjectionInEmail() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpRequestDto("'; DROP TABLE users;--"))))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should reject SQL injection in OTP email field")
        void shouldRejectSqlInjectionInOtpEmail() throws Exception {
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("' OR '1'='1", "123456"))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("OTP brute-force protection")
    class BruteForceProtection {

        @Test
        @DisplayName("should rate-limit OTP requests to prevent brute-force")
        void shouldRateLimitOtpRequests() throws Exception {
            String email = "brute@test.com";
            String body = objectMapper.writeValueAsString(new OtpRequestDto(email));

            // Send 3 requests (within limit)
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
            }

            // 4th should be blocked
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    @DisplayName("JWT alg=none attack")
    class JwtAlgNone {

        @Test
        @DisplayName("should reject JWT with alg=none")
        void shouldRejectAlgNone() throws Exception {
            String header = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes());
            String payload = java.util.Base64.getUrlEncoder().withoutPadding()
                    .encodeToString("{\"sub\":\"hacker@test.com\",\"exp\":9999999999}".getBytes());
            String unsignedToken = header + "." + payload + ".";

            mockMvc.perform(get("/api/protected")
                            .header("Authorization", "Bearer " + unsignedToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("CORS protection")
    class CorsProtection {

        @Test
        @DisplayName("should allow requests from configured origin")
        void shouldAllowConfiguredOrigin() throws Exception {
            mockMvc.perform(options("/auth/otp/request")
                            .header("Origin", "http://localhost:5173")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
        }

        @Test
        @DisplayName("should block requests from unconfigured origin")
        void shouldBlockUnconfiguredOrigin() throws Exception {
            mockMvc.perform(options("/auth/otp/request")
                            .header("Origin", "http://evil.com")
                            .header("Access-Control-Request-Method", "POST"))
                    .andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
        }
    }

    @Nested
    @DisplayName("Auth endpoint protection")
    class AuthEndpointProtection {

        @Test
        @DisplayName("should allow unauthenticated access to auth endpoints")
        void shouldAllowUnauthAccessToAuthEndpoints() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpRequestDto("public@test.com"))))
                    .andExpect(status().isAccepted());
        }

        @Test
        @DisplayName("should require authentication for non-auth endpoints")
        void shouldRequireAuthForProtectedEndpoints() throws Exception {
            mockMvc.perform(get("/api/anything"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Error response security")
    class ErrorResponseSecurity {

        @Test
        @DisplayName("should never expose stack traces in error responses")
        void shouldNotExposeStackTraces() throws Exception {
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"test@test.com\", \"otp\": \"123456\"}"))
                    .andExpect(jsonPath("$.trace").doesNotExist())
                    .andExpect(jsonPath("$.stackTrace").doesNotExist());
        }
    }
}
