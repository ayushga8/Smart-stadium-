package com.smartstadium.service;

import com.smartstadium.dto.AuthResponseDto;
import com.smartstadium.entity.AuthProvider;
import com.smartstadium.entity.User;
import com.smartstadium.entity.UserRole;
import com.smartstadium.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    @Value("${app.admin-email}")
    private String adminEmail;

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;

    public AuthResponseDto verifyOtpAndLogin(String email, String otp) {
        otpService.verify(email, otp);

        User user = userRepository.findByEmail(email.toLowerCase())
                .map(existing -> {
                    existing.setAuthProvider(AuthProvider.LOCAL);
                    existing.setLastLoginAt(LocalDateTime.now());
                    assignRoleIfNeeded(existing);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email.toLowerCase())
                            .name(email.substring(0, email.indexOf('@')))
                            .authProvider(AuthProvider.LOCAL)
                            .build();
                    assignRoleIfNeeded(newUser);
                    return userRepository.save(newUser);
                });

        return buildAuthResponse(user);
    }

    public AuthResponseDto processGoogleLogin(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        User user = userRepository.findByEmail(email.toLowerCase())
                .map(existing -> {
                    existing.setAuthProvider(AuthProvider.GOOGLE);
                    existing.setName(name);
                    existing.setLastLoginAt(LocalDateTime.now());
                    assignRoleIfNeeded(existing);
                    return userRepository.save(existing);
                })
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email.toLowerCase())
                            .name(name)
                            .authProvider(AuthProvider.GOOGLE)
                            .build();
                    assignRoleIfNeeded(newUser);
                    return userRepository.save(newUser);
                });

        return buildAuthResponse(user);
    }

    private void assignRoleIfNeeded(User user) {
        if (adminEmail.equalsIgnoreCase(user.getEmail())) {
            user.setRole(UserRole.ADMIN);
        }
    }

    private AuthResponseDto buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }
}
