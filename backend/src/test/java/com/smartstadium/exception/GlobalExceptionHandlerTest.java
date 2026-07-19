package com.smartstadium.exception;

import com.smartstadium.dto.ErrorResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Global Exception Handler")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("UserNotFoundException")
    class UserNotFound {
        @Test
        @DisplayName("returns 404 with user not found message")
        void returns404() {
            ResponseEntity<ErrorResponseDto> response = handler.handleUserNotFound(
                new UserNotFoundException("test@email.com"));
            assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
            assertEquals("User Not Found", response.getBody().getError());
            assertTrue(response.getBody().getMessage().contains("test@email.com"));
        }

        @Test
        @DisplayName("response has timestamp")
        void hasTimestamp() {
            ResponseEntity<ErrorResponseDto> response = handler.handleUserNotFound(
                new UserNotFoundException("x@y.com"));
            assertNotNull(response.getBody().getTimestamp());
        }
    }

    @Nested
    @DisplayName("InvalidOtpException")
    class InvalidOtp {
        @Test
        @DisplayName("returns 401")
        void returns401() {
            ResponseEntity<ErrorResponseDto> response = handler.handleInvalidOtp(
                new InvalidOtpException("Bad OTP"));
            assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatusCode().value());
            assertEquals("Invalid OTP", response.getBody().getError());
        }
    }

    @Nested
    @DisplayName("OtpExpiredException")
    class OtpExpired {
        @Test
        @DisplayName("returns 410 Gone")
        void returns410() {
            ResponseEntity<ErrorResponseDto> response = handler.handleOtpExpired(
                new OtpExpiredException("Expired"));
            assertEquals(HttpStatus.GONE.value(), response.getStatusCode().value());
            assertEquals("OTP Expired", response.getBody().getError());
        }
    }

    @Nested
    @DisplayName("RateLimitExceededException")
    class RateLimit {
        @Test
        @DisplayName("returns 429")
        void returns429() {
            ResponseEntity<ErrorResponseDto> response = handler.handleRateLimit(
                new RateLimitExceededException("Too fast"));
            assertEquals(HttpStatus.TOO_MANY_REQUESTS.value(), response.getStatusCode().value());
            assertEquals("Rate Limit Exceeded", response.getBody().getError());
        }
    }

    @Nested
    @DisplayName("ProviderMismatchException")
    class ProviderMismatch {
        @Test
        @DisplayName("returns 409 Conflict")
        void returns409() {
            ResponseEntity<ErrorResponseDto> response = handler.handleProviderMismatch(
                new ProviderMismatchException("Use Google"));
            assertEquals(HttpStatus.CONFLICT.value(), response.getStatusCode().value());
            assertEquals("Provider Mismatch", response.getBody().getError());
        }
    }

    @Nested
    @DisplayName("MethodArgumentNotValidException")
    class Validation {
        @Test
        @DisplayName("returns 400 with field errors")
        void returns400() {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "email", "Email is required")));
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            ResponseEntity<ErrorResponseDto> response = handler.handleValidation(ex);
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
            assertTrue(response.getBody().getMessage().contains("email"));
        }

        @Test
        @DisplayName("concatenates multiple field errors")
        void multipleFieldErrors() {
            BindingResult bindingResult = mock(BindingResult.class);
            when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("dto", "email", "required"),
                new FieldError("dto", "otp", "too short")));
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            ResponseEntity<ErrorResponseDto> response = handler.handleValidation(ex);
            assertTrue(response.getBody().getMessage().contains("email"));
            assertTrue(response.getBody().getMessage().contains("otp"));
        }
    }

    @Nested
    @DisplayName("HttpMessageNotReadableException")
    class MalformedJson {
        @Test
        @DisplayName("returns 400 for malformed JSON")
        void returns400() {
            HttpMessageNotReadableException ex = mock(HttpMessageNotReadableException.class);
            ResponseEntity<ErrorResponseDto> response = handler.handleMalformedJson(ex);
            assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
            assertTrue(response.getBody().getMessage().contains("Invalid JSON"));
        }
    }

    @Nested
    @DisplayName("General Exception")
    class General {
        @Test
        @DisplayName("returns 500 for unexpected errors")
        void returns500() {
            ResponseEntity<ErrorResponseDto> response = handler.handleGeneral(
                new RuntimeException("unexpected"));
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
            assertEquals("Internal Server Error", response.getBody().getError());
        }

        @Test
        @DisplayName("does not expose internal error details")
        void hidesDetails() {
            ResponseEntity<ErrorResponseDto> response = handler.handleGeneral(
                new RuntimeException("SQL injection attempt"));
            assertFalse(response.getBody().getMessage().contains("SQL"));
        }
    }

    @Nested
    @DisplayName("Response structure")
    class ResponseStructure {
        @Test
        @DisplayName("all responses have status, error, message, timestamp")
        void allFieldsPresent() {
            ResponseEntity<ErrorResponseDto> response = handler.handleUserNotFound(
                new UserNotFoundException("test@x.com"));
            ErrorResponseDto body = response.getBody();
            assertNotNull(body);
            assertTrue(body.getStatus() > 0);
            assertNotNull(body.getError());
            assertNotNull(body.getMessage());
            assertNotNull(body.getTimestamp());
        }

        @Test
        @DisplayName("status code matches body status")
        void statusCodesMatch() {
            ResponseEntity<ErrorResponseDto> response = handler.handleRateLimit(
                new RateLimitExceededException("test"));
            assertEquals(response.getStatusCode().value(), response.getBody().getStatus());
        }
    }
}
