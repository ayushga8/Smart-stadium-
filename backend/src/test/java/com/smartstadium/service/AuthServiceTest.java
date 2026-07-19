package com.smartstadium.service;

import com.smartstadium.dto.AuthResponseDto;
import com.smartstadium.entity.AuthProvider;
import com.smartstadium.entity.User;
import com.smartstadium.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private OtpService otpService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("OTP verification login")
    class OtpLogin {

        @Test
        @DisplayName("should create a new user on first OTP verification")
        void shouldCreateNewUserOnFirstVerify() {
            when(otpService.verify(anyString(), anyString())).thenReturn(true);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("access-token");

            AuthResponseDto response = authService.verifyOtpAndLogin("new@test.com", "123456");

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();

            assertThat(savedUser.getEmail()).isEqualTo("new@test.com");
            assertThat(savedUser.getAuthProvider()).isEqualTo(AuthProvider.LOCAL);
            assertThat(response.getAccessToken()).isEqualTo("access-token");
        }

        @Test
        @DisplayName("should update lastLoginAt for existing user")
        void shouldUpdateLastLoginAt() {
            User existing = User.builder()
                    .id(1L)
                    .email("existing@test.com")
                    .name("existing")
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(otpService.verify(anyString(), anyString())).thenReturn(true);
            when(userRepository.findByEmail("existing@test.com")).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("existing@test.com", "123456");

            verify(userRepository).save(argThat(user ->
                    user.getLastLoginAt().isAfter(LocalDateTime.now().minusSeconds(5))));
        }

        @Test
        @DisplayName("should set authProvider to LOCAL on OTP login")
        void shouldSetProviderToLocal() {
            when(otpService.verify(anyString(), anyString())).thenReturn(true);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("test@test.com", "123456");

            verify(userRepository).save(argThat(user ->
                    user.getAuthProvider() == AuthProvider.LOCAL));
        }

        @Test
        @DisplayName("should return AuthResponseDto with email and access token")
        void shouldReturnAuthResponse() {
            when(otpService.verify(anyString(), anyString())).thenReturn(true);
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("my-access-token");

            AuthResponseDto response = authService.verifyOtpAndLogin("test@test.com", "123456");

            assertThat(response.getAccessToken()).isEqualTo("my-access-token");
            assertThat(response.getEmail()).isEqualTo("test@test.com");
        }

        @Test
        @DisplayName("should link accounts when Google user logs in via OTP — sets provider to LOCAL")
        void shouldLinkGoogleAccountViaOtp() {
            User googleUser = User.builder()
                    .id(1L)
                    .email("google@test.com")
                    .name("Google User")
                    .authProvider(AuthProvider.GOOGLE)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(otpService.verify(anyString(), anyString())).thenReturn(true);
            when(userRepository.findByEmail("google@test.com")).thenReturn(Optional.of(googleUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("google@test.com", "123456");

            verify(userRepository).save(argThat(user ->
                    user.getAuthProvider() == AuthProvider.LOCAL));
        }
    }

    @Nested
    @DisplayName("Google login")
    class GoogleLogin {

        @Test
        @DisplayName("should create new user on first Google login")
        void shouldCreateNewUserOnGoogleLogin() {
            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn("google@test.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Google User");

            when(userRepository.findByEmail("google@test.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            AuthResponseDto response = authService.processGoogleLogin(oAuth2User);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            User saved = captor.getValue();

            assertThat(saved.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
            assertThat(saved.getName()).isEqualTo("Google User");
            assertThat(saved.getEmail()).isEqualTo("google@test.com");
        }

        @Test
        @DisplayName("should set authProvider to GOOGLE on Google login")
        void shouldSetProviderToGoogle() {
            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn("google@test.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Google User");

            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.processGoogleLogin(oAuth2User);

            verify(userRepository).save(argThat(u -> u.getAuthProvider() == AuthProvider.GOOGLE));
        }

        @Test
        @DisplayName("should link accounts when LOCAL user logs in via Google — sets provider to GOOGLE")
        void shouldLinkLocalAccountViaGoogle() {
            User localUser = User.builder()
                    .id(1L)
                    .email("local@test.com")
                    .name("local")
                    .authProvider(AuthProvider.LOCAL)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn("local@test.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Local User via Google");

            when(userRepository.findByEmail("local@test.com")).thenReturn(Optional.of(localUser));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.processGoogleLogin(oAuth2User);

            verify(userRepository).save(argThat(user -> {
                return user.getAuthProvider() == AuthProvider.GOOGLE
                        && user.getName().equals("Local User via Google");
            }));
        }

        @Test
        @DisplayName("should update name from Google profile on existing user")
        void shouldUpdateNameFromGoogle() {
            User existing = User.builder()
                    .id(1L)
                    .email("user@test.com")
                    .name("Old Name")
                    .authProvider(AuthProvider.GOOGLE)
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn("user@test.com");
            when(oAuth2User.getAttribute("name")).thenReturn("Updated Name");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.processGoogleLogin(oAuth2User);

            verify(userRepository).save(argThat(u -> u.getName().equals("Updated Name")));
        }

        @Test
        @DisplayName("should update lastLoginAt on Google login for existing user")
        void shouldUpdateLastLoginAtOnGoogleLogin() {
            User existing = User.builder()
                    .id(1L)
                    .email("user@test.com")
                    .name("User")
                    .authProvider(AuthProvider.GOOGLE)
                    .createdAt(LocalDateTime.now().minusDays(30))
                    .lastLoginAt(LocalDateTime.now().minusDays(5))
                    .build();

            OAuth2User oAuth2User = mock(OAuth2User.class);
            when(oAuth2User.getAttribute("email")).thenReturn("user@test.com");
            when(oAuth2User.getAttribute("name")).thenReturn("User");

            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(existing));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.processGoogleLogin(oAuth2User);

            verify(userRepository).save(argThat(u ->
                    u.getLastLoginAt().isAfter(LocalDateTime.now().minusSeconds(5))));
        }
    }
}
