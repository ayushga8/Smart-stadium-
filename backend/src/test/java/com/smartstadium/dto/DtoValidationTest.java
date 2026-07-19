package com.smartstadium.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DTO Validation")
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("OtpRequestDto")
    class OtpRequestTests {

        @Test
        @DisplayName("valid email passes validation")
        void validEmail() {
            OtpRequestDto dto = new OtpRequestDto("test@example.com");
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty());
        }

        @ParameterizedTest
        @DisplayName("blank email fails validation")
        @NullAndEmptySource
        void blankEmailFails(String email) {
            OtpRequestDto dto = new OtpRequestDto(email);
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(dto);
            assertFalse(violations.isEmpty());
        }

        @ParameterizedTest
        @DisplayName("invalid email format fails validation")
        @ValueSource(strings = {"notanemail", "missing@", "@nodomain", "spaces in@email.com"})
        void invalidEmailFails(String email) {
            OtpRequestDto dto = new OtpRequestDto(email);
            Set<ConstraintViolation<OtpRequestDto>> violations = validator.validate(dto);
            assertFalse(violations.isEmpty());
        }

        @Test
        @DisplayName("getter and setter work")
        void getterSetter() {
            OtpRequestDto dto = new OtpRequestDto();
            dto.setEmail("test@test.com");
            assertEquals("test@test.com", dto.getEmail());
        }
    }

    @Nested
    @DisplayName("OtpVerifyDto")
    class OtpVerifyTests {

        @Test
        @DisplayName("valid email and OTP passes")
        void validDto() {
            OtpVerifyDto dto = new OtpVerifyDto("test@example.com", "123456");
            Set<ConstraintViolation<OtpVerifyDto>> violations = validator.validate(dto);
            assertTrue(violations.isEmpty());
        }

        @Test
        @DisplayName("null email fails")
        void nullEmailFails() {
            OtpVerifyDto dto = new OtpVerifyDto(null, "123456");
            assertFalse(validator.validate(dto).isEmpty());
        }

        @Test
        @DisplayName("null OTP fails")
        void nullOtpFails() {
            OtpVerifyDto dto = new OtpVerifyDto("test@example.com", null);
            assertFalse(validator.validate(dto).isEmpty());
        }

        @Test
        @DisplayName("OTP shorter than 6 digits fails")
        void shortOtpFails() {
            OtpVerifyDto dto = new OtpVerifyDto("test@example.com", "123");
            assertFalse(validator.validate(dto).isEmpty());
        }

        @Test
        @DisplayName("OTP longer than 6 digits fails")
        void longOtpFails() {
            OtpVerifyDto dto = new OtpVerifyDto("test@example.com", "1234567");
            assertFalse(validator.validate(dto).isEmpty());
        }

        @Test
        @DisplayName("exactly 6 digit OTP passes")
        void exactOtpPasses() {
            OtpVerifyDto dto = new OtpVerifyDto("test@example.com", "000000");
            assertTrue(validator.validate(dto).isEmpty());
        }

        @Test
        @DisplayName("blank OTP fails")
        void blankOtpFails() {
            OtpVerifyDto dto = new OtpVerifyDto("test@example.com", "");
            assertFalse(validator.validate(dto).isEmpty());
        }

        @Test
        @DisplayName("invalid email with valid OTP fails")
        void invalidEmailWithValidOtp() {
            OtpVerifyDto dto = new OtpVerifyDto("notanemail", "123456");
            assertFalse(validator.validate(dto).isEmpty());
        }
    }

    @Nested
    @DisplayName("AuthResponseDto")
    class AuthResponseTests {

        @Test
        @DisplayName("builder creates DTO with all fields")
        void builderWorks() {
            AuthResponseDto dto = AuthResponseDto.builder()
                .accessToken("token123")
                .email("test@example.com")
                .name("Test User")
                .role("ADMIN")
                .build();

            assertEquals("token123", dto.getAccessToken());
            assertEquals("test@example.com", dto.getEmail());
            assertEquals("Test User", dto.getName());
            assertEquals("ADMIN", dto.getRole());
        }

        @Test
        @DisplayName("no-args constructor works")
        void noArgConstructor() {
            AuthResponseDto dto = new AuthResponseDto();
            assertNull(dto.getAccessToken());
        }
    }

    @Nested
    @DisplayName("ChatRequestDto")
    class ChatRequestTests {

        @Test
        @DisplayName("getter and setter work")
        void getterSetter() {
            ChatRequestDto dto = new ChatRequestDto();
            dto.setMessage("Hello AI");
            assertEquals("Hello AI", dto.getMessage());
        }
    }

    @Nested
    @DisplayName("ChatResponseDto")
    class ChatResponseTests {

        @Test
        @DisplayName("builder creates DTO with all fields")
        void builderWorks() {
            LocalDateTime now = LocalDateTime.now();
            ChatResponseDto dto = ChatResponseDto.builder()
                .response("Hi there!")
                .language("en")
                .timestamp(now)
                .build();

            assertEquals("Hi there!", dto.getResponse());
            assertEquals("en", dto.getLanguage());
            assertEquals(now, dto.getTimestamp());
        }
    }

    @Nested
    @DisplayName("ErrorResponseDto")
    class ErrorResponseTests {

        @Test
        @DisplayName("builder creates DTO with all fields")
        void builderWorks() {
            LocalDateTime now = LocalDateTime.now();
            ErrorResponseDto dto = ErrorResponseDto.builder()
                .status(404)
                .error("Not Found")
                .message("User not found")
                .timestamp(now)
                .build();

            assertEquals(404, dto.getStatus());
            assertEquals("Not Found", dto.getError());
            assertEquals("User not found", dto.getMessage());
            assertEquals(now, dto.getTimestamp());
        }

        @Test
        @DisplayName("all-args constructor works")
        void allArgsConstructor() {
            LocalDateTime now = LocalDateTime.now();
            ErrorResponseDto dto = new ErrorResponseDto(500, "Error", "msg", now);
            assertEquals(500, dto.getStatus());
        }
    }
}
