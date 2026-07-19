package com.smartstadium.controller;

import com.smartstadium.entity.AuthProvider;
import com.smartstadium.entity.User;
import com.smartstadium.entity.UserRole;
import com.smartstadium.repository.UserRepository;
import com.smartstadium.service.JwtService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        if (userRepository.findByEmail("profile@test.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("profile@test.com")
                    .name("Profile Test")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.USER)
                    .build());
        }
        if (userRepository.findByEmail("ayushgdg18@gmail.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("ayushgdg18@gmail.com")
                    .name("Admin")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.ADMIN)
                    .build());
        }
    }

    @Nested
    @DisplayName("GET /api/user/profile")
    class UserProfile {

        @Test
        @DisplayName("should return user profile with all fields")
        void profileHasAllFields() throws Exception {
            String token = jwtService.generateAccessToken("profile@test.com", "USER");

            mockMvc.perform(get("/api/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("profile@test.com"))
                    .andExpect(jsonPath("$.name").value("Profile Test"))
                    .andExpect(jsonPath("$.role", notNullValue()));
        }

        @Test
        @DisplayName("should return USER role for regular user")
        void regularUserHasUserRole() throws Exception {
            String token = jwtService.generateAccessToken("profile@test.com", "USER");

            mockMvc.perform(get("/api/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("should return ADMIN role for admin user")
        void adminUserHasAdminRole() throws Exception {
            String token = jwtService.generateAccessToken("ayushgdg18@gmail.com", "ADMIN");

            mockMvc.perform(get("/api/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("should return 401 without token")
        void noTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/user/profile"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return 401 with invalid token")
        void invalidTokenReturns401() throws Exception {
            mockMvc.perform(get("/api/user/profile")
                            .header("Authorization", "Bearer garbage.token.here"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should return correct authProvider")
        void profileHasAuthProvider() throws Exception {
            String token = jwtService.generateAccessToken("profile@test.com", "USER");

            mockMvc.perform(get("/api/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.authProvider").value("LOCAL"));
        }

        @Test
        @DisplayName("should return email matching JWT subject")
        void emailMatchesJwtSubject() throws Exception {
            String token = jwtService.generateAccessToken("profile@test.com", "USER");

            mockMvc.perform(get("/api/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("profile@test.com"));
        }

        @Test
        @DisplayName("should return createdAt timestamp")
        void profileHasCreatedAt() throws Exception {
            String token = jwtService.generateAccessToken("profile@test.com", "USER");

            mockMvc.perform(get("/api/user/profile")
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.createdAt", notNullValue()));
        }
    }
}
