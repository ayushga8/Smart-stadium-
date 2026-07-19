package com.smartstadium.service;

import com.smartstadium.dto.AuthResponseDto;
import com.smartstadium.entity.AuthProvider;
import com.smartstadium.entity.User;
import com.smartstadium.entity.UserRole;
import com.smartstadium.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Role assignment logic")
class RoleAssignmentTest {

    @Mock private UserRepository userRepository;
    @Mock private OtpService otpService;
    @Mock private JwtService jwtService;
    @InjectMocks private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "adminEmail", "ayushgdg18@gmail.com");
    }

    @Nested
    @DisplayName("Admin auto-assignment on OTP login")
    class AdminAutoAssignment {

        @Test
        @DisplayName("should assign ADMIN role when admin email logs in via OTP")
        void adminEmailGetsAdminRole() {
            when(userRepository.findByEmail("ayushgdg18@gmail.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            AuthResponseDto response = authService.verifyOtpAndLogin("ayushgdg18@gmail.com", "123456");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("should assign USER role for non-admin email")
        void nonAdminEmailGetsUserRole() {
            when(userRepository.findByEmail("someone@test.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("someone@test.com", "123456");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("should handle admin email case-insensitively")
        void adminEmailCaseInsensitive() {
            when(userRepository.findByEmail("ayushgdg18@gmail.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("AyushGDG18@Gmail.com", "123456");

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("should include role in auth response")
        void responseContainsRole() {
            when(userRepository.findByEmail("ayushgdg18@gmail.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            AuthResponseDto response = authService.verifyOtpAndLogin("ayushgdg18@gmail.com", "123456");

            assertThat(response.getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("should return USER role in response for non-admin")
        void responseContainsUserRole() {
            when(userRepository.findByEmail("person@test.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            AuthResponseDto response = authService.verifyOtpAndLogin("person@test.com", "123456");

            assertThat(response.getRole()).isEqualTo("USER");
        }

        @Test
        @DisplayName("should preserve ADMIN role on re-login")
        void adminRolePreservedOnReLogin() {
            User existingAdmin = User.builder()
                    .email("ayushgdg18@gmail.com")
                    .name("Admin")
                    .authProvider(AuthProvider.GOOGLE)
                    .role(UserRole.ADMIN)
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(userRepository.findByEmail("ayushgdg18@gmail.com"))
                    .thenReturn(Optional.of(existingAdmin));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("ayushgdg18@gmail.com", "123456");

            verify(userRepository).save(argThat(u -> u.getRole() == UserRole.ADMIN));
        }

        @Test
        @DisplayName("should not promote regular user to admin on re-login")
        void regularUserStaysRegular() {
            User existingUser = User.builder()
                    .email("regular@test.com")
                    .name("Regular")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.USER)
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(userRepository.findByEmail("regular@test.com"))
                    .thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("regular@test.com", "123456");

            verify(userRepository).save(argThat(u -> u.getRole() == UserRole.USER));
        }

        @Test
        @DisplayName("should preserve VOLUNTEER role on re-login for non-admin")
        void volunteerRolePreserved() {
            User volunteer = User.builder()
                    .email("vol@test.com")
                    .name("Volunteer")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.VOLUNTEER)
                    .lastLoginAt(LocalDateTime.now().minusDays(1))
                    .build();

            when(userRepository.findByEmail("vol@test.com"))
                    .thenReturn(Optional.of(volunteer));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("vol@test.com", "123456");

            verify(userRepository).save(argThat(u -> u.getRole() == UserRole.VOLUNTEER));
        }
    }

    @Nested
    @DisplayName("JWT token generation with role")
    class TokenWithRole {

        @Test
        @DisplayName("should call generateAccessToken with correct role for admin")
        void adminTokenGeneratedWithAdminRole() {
            when(userRepository.findByEmail("ayushgdg18@gmail.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("ayushgdg18@gmail.com", "123456");

            verify(jwtService).generateAccessToken("ayushgdg18@gmail.com", "ADMIN");
        }

        @Test
        @DisplayName("should call generateAccessToken with USER role for regular user")
        void userTokenGeneratedWithUserRole() {
            when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("user@test.com", "123456");

            verify(jwtService).generateAccessToken("user@test.com", "USER");
        }

        @Test
        @DisplayName("should call generateAccessToken with VOLUNTEER role")
        void volunteerTokenGeneratedWithVolunteerRole() {
            User volunteer = User.builder()
                    .email("vol2@test.com")
                    .name("Vol")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.VOLUNTEER)
                    .lastLoginAt(LocalDateTime.now())
                    .build();

            when(userRepository.findByEmail("vol2@test.com"))
                    .thenReturn(Optional.of(volunteer));
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(anyString(), anyString())).thenReturn("token");

            authService.verifyOtpAndLogin("vol2@test.com", "123456");

            verify(jwtService).generateAccessToken("vol2@test.com", "VOLUNTEER");
        }
    }
}
