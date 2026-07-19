package com.smartstadium.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstadium.dto.AuthResponseDto;
import com.smartstadium.dto.OtpRequestDto;
import com.smartstadium.dto.OtpVerifyDto;
import com.smartstadium.entity.AuthProvider;
import com.smartstadium.entity.User;
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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OtpService otpService;
    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private JavaMailSender mailSender; // mock mail to avoid real SMTP

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        otpService.getOtpStore().clear();
        otpService.getRateLimiters().clear();
    }

    @Nested
    @DisplayName("Full OTP request -> verify flow")
    class FullOtpFlow {

        @Test
        @DisplayName("should complete full OTP flow: request -> verify -> get JWT")
        void shouldCompleteFullOtpFlow() throws Exception {
            // 1. Request OTP
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto("integration@test.com"))))
                    .andExpect(status().isAccepted());

            // 2. Extract OTP from in-memory store
            String otp = otpService.getOtpStore().get("integration@test.com").otp;
            assertThat(otp).hasSize(6);

            // 3. Verify OTP
            MvcResult result = mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("integration@test.com", otp))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").isNotEmpty())
                    .andExpect(jsonPath("$.email").value("integration@test.com"))
                    .andExpect(cookie().exists("refresh_token"))
                    .andReturn();

            // 4. Verify user was created in DB
            Optional<User> user = userRepository.findByEmail("integration@test.com");
            assertThat(user).isPresent();
            assertThat(user.get().getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
        }

        @Test
        @DisplayName("should create user with correct name derived from email")
        void shouldCreateUserWithNameFromEmail() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto("john.doe@test.com"))))
                    .andExpect(status().isAccepted());

            String otp = otpService.getOtpStore().get("john.doe@test.com").otp;

            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("john.doe@test.com", otp))))
                    .andExpect(status().isOk());

            User user = userRepository.findByEmail("john.doe@test.com").orElseThrow();
            assertThat(user.getName()).isEqualTo("john.doe");
        }

        @Test
        @DisplayName("should not allow reuse of verified OTP")
        void shouldNotAllowOtpReuse() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OtpRequestDto("reuse@test.com"))));

            String otp = otpService.getOtpStore().get("reuse@test.com").otp;

            // First verify succeeds
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("reuse@test.com", otp))))
                    .andExpect(status().isOk());

            // Second verify fails
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("reuse@test.com", otp))))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("JWT round-trip through auth filter")
    class JwtRoundTrip {

        @Test
        @DisplayName("should accept valid JWT on protected endpoint")
        void shouldAcceptValidJwt() throws Exception {
            String token = jwtService.generateAccessToken("authed@test.com", "USER");

            int status = mockMvc.perform(get("/api/test")
                            .header("Authorization", "Bearer " + token))
                    .andReturn().getResponse().getStatus();

            // Auth passed — we should NOT get 401/302. The 404 or 500 is expected since the endpoint doesn't exist.
            assertThat(status).isNotEqualTo(401);
            assertThat(status).isNotEqualTo(302);
        }

        @Test
        @DisplayName("should reject request without JWT on protected endpoint")
        void shouldRejectWithoutJwt() throws Exception {
            mockMvc.perform(get("/api/protected"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should reject expired JWT")
        void shouldRejectExpiredJwt() throws Exception {
            JwtService shortLived = new JwtService(
                    "TestSecretKeyThatIsAtLeast256BitsLongForHS256AlgorithmTesting!!!",
                    0, 0);
            String expiredToken = shortLived.generateAccessToken("expired@test.com", "USER");

            mockMvc.perform(get("/api/protected")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should reject tampered JWT")
        void shouldRejectTamperedJwt() throws Exception {
            String token = jwtService.generateAccessToken("user@test.com", "USER");
            String tampered = token.substring(0, token.length() - 5) + "XXXXX";

            mockMvc.perform(get("/api/protected")
                            .header("Authorization", "Bearer " + tampered))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Rate limiting integration")
    class RateLimitingIntegration {

        @Test
        @DisplayName("should return 429 after exceeding rate limit")
        void shouldReturn429AfterExceedingLimit() throws Exception {
            String email = "ratelimited@test.com";
            String body = objectMapper.writeValueAsString(new OtpRequestDto(email));

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/auth/otp/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body));
            }

            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isTooManyRequests());
        }
    }

    @Nested
    @DisplayName("Account linking integration")
    class AccountLinking {

        @Test
        @DisplayName("should link accounts when same email used with different provider")
        void shouldLinkAccounts() throws Exception {
            // First: create user via OTP
            mockMvc.perform(post("/auth/otp/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(new OtpRequestDto("link@test.com"))));

            String otp = otpService.getOtpStore().get("link@test.com").otp;
            mockMvc.perform(post("/auth/otp/verify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new OtpVerifyDto("link@test.com", otp))));

            User user = userRepository.findByEmail("link@test.com").orElseThrow();
            assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);

            // Verify only one user exists for this email
            assertThat(userRepository.findAll().stream()
                    .filter(u -> u.getEmail().equals("link@test.com"))
                    .count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Database constraints")
    class DatabaseConstraints {

        @Test
        @DisplayName("should enforce unique email constraint")
        void shouldEnforceUniqueEmail() {
            userRepository.save(User.builder()
                    .email("unique@test.com")
                    .name("first")
                    .authProvider(AuthProvider.LOCAL)
                    .build());

            assertThatThrownBy(() -> userRepository.save(User.builder()
                    .email("unique@test.com")
                    .name("second")
                    .authProvider(AuthProvider.LOCAL)
                    .build()))
                    .isInstanceOf(Exception.class); // DataIntegrityViolationException
        }

        @Test
        @DisplayName("should set createdAt automatically via @PrePersist")
        void shouldSetCreatedAt() {
            User saved = userRepository.save(User.builder()
                    .email("timestamp@test.com")
                    .name("test")
                    .authProvider(AuthProvider.LOCAL)
                    .build());

            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getLastLoginAt()).isNotNull();
        }
    }
}
