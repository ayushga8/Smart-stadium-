package com.smartstadium.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartstadium.dto.AuthResponseDto;
import com.smartstadium.dto.OtpRequestDto;
import com.smartstadium.dto.OtpVerifyDto;
import com.smartstadium.exception.GlobalExceptionHandler;
import com.smartstadium.exception.InvalidOtpException;
import com.smartstadium.exception.OtpExpiredException;
import com.smartstadium.exception.RateLimitExceededException;
import com.smartstadium.service.AuthService;
import com.smartstadium.service.JwtService;
import com.smartstadium.service.OtpService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OtpService otpService;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;

    @Nested
    @DisplayName("POST /auth/otp/request")
    class OtpRequest {

        @Test
        @DisplayName("should return 202 for valid email")
        void shouldReturn202ForValidEmail() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto("test@example.com"))))
                    .andExpect(status().isAccepted());

            verify(otpService).generateAndSend("test@example.com");
        }

        @Test
        @DisplayName("should return 400 for invalid email format")
        void shouldReturn400ForInvalidEmail() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto("not-an-email"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation Error"));
        }

        @Test
        @DisplayName("should return 400 for empty email")
        void shouldReturn400ForEmptyEmail() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto(""))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Validation Error"));
        }

        @Test
        @DisplayName("should return 400 for null email")
        void shouldReturn400ForNullEmail() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": null}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for missing email field")
        void shouldReturn400ForMissingField() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 429 when rate limited")
        void shouldReturn429WhenRateLimited() throws Exception {
            doThrow(new RateLimitExceededException("Too many requests"))
                    .when(otpService).generateAndSend(anyString());

            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto("test@example.com"))))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.status").value(429))
                    .andExpect(jsonPath("$.error").value("Rate Limit Exceeded"));
        }

        @Test
        @DisplayName("should return JSON content type for error responses")
        void shouldReturnJsonContentType() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto("bad"))))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("should return 400 for malformed JSON")
        void shouldReturn400ForMalformedJson() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{not valid json"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Malformed Request"));
        }

        @Test
        @DisplayName("error response should have consistent shape")
        void errorResponseShouldHaveConsistentShape() throws Exception {
            mockMvc.perform(post("/auth/otp/request")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new OtpRequestDto("bad-email"))))
                    .andExpect(jsonPath("$.status").isNumber())
                    .andExpect(jsonPath("$.error").isString())
                    .andExpect(jsonPath("$.message").isString())
                    .andExpect(jsonPath("$.timestamp").isString());
        }
    }

    @Nested
    @DisplayName("POST /auth/otp/verify")
    class OtpVerify {

        @Test
        @DisplayName("should return 200 with auth response for correct OTP")
        void shouldReturn200ForCorrectOtp() throws Exception {
            AuthResponseDto authResponse = AuthResponseDto.builder()
                    .accessToken("access-token")
                    .email("test@example.com")
                    .name("test")
                    .build();

            when(authService.verifyOtpAndLogin("test@example.com", "123456")).thenReturn(authResponse);
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("test@example.com", "123456"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token"))
                    .andExpect(jsonPath("$.email").value("test@example.com"))
                    .andExpect(cookie().exists("refresh_token"));
        }

        @Test
        @DisplayName("should set refresh token as HttpOnly cookie")
        void shouldSetHttpOnlyCookie() throws Exception {
            AuthResponseDto authResponse = AuthResponseDto.builder()
                    .accessToken("token")
                    .email("test@example.com")
                    .name("test")
                    .build();

            when(authService.verifyOtpAndLogin(anyString(), anyString())).thenReturn(authResponse);
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh-token");

            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("test@example.com", "123456"))))
                    .andExpect(cookie().httpOnly("refresh_token", true));
        }

        @Test
        @DisplayName("should return 401 for incorrect OTP")
        void shouldReturn401ForIncorrectOtp() throws Exception {
            when(authService.verifyOtpAndLogin(anyString(), anyString()))
                    .thenThrow(new InvalidOtpException("Invalid OTP code"));

            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("test@example.com", "000000"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid OTP"));
        }

        @Test
        @DisplayName("should return 410 for expired OTP")
        void shouldReturn410ForExpiredOtp() throws Exception {
            when(authService.verifyOtpAndLogin(anyString(), anyString()))
                    .thenThrow(new OtpExpiredException("OTP has expired"));

            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("test@example.com", "123456"))))
                    .andExpect(status().isGone())
                    .andExpect(jsonPath("$.error").value("OTP Expired"));
        }

        @Test
        @DisplayName("should return 400 for invalid email in verify request")
        void shouldReturn400ForInvalidEmailInVerify() throws Exception {
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"not-valid\", \"otp\": \"123456\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for missing OTP field")
        void shouldReturn400ForMissingOtp() throws Exception {
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"test@example.com\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for OTP with wrong length")
        void shouldReturn400ForWrongLengthOtp() throws Exception {
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"test@example.com\", \"otp\": \"12345\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 for OTP longer than 6 characters")
        void shouldReturn400ForTooLongOtp() throws Exception {
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\": \"test@example.com\", \"otp\": \"1234567\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return JSON content type for success response")
        void shouldReturnJsonForSuccess() throws Exception {
            AuthResponseDto authResponse = AuthResponseDto.builder()
                    .accessToken("token").email("test@example.com").name("test").build();
            when(authService.verifyOtpAndLogin(anyString(), anyString())).thenReturn(authResponse);
            when(jwtService.generateRefreshToken(anyString())).thenReturn("refresh");

            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new OtpVerifyDto("test@example.com", "123456"))))
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("should return 400 for malformed JSON in verify request")
        void shouldReturn400ForMalformedJsonInVerify() throws Exception {
            mockMvc.perform(post("/auth/otp/verify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{{invalid"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Malformed Request"));
        }
    }
}
