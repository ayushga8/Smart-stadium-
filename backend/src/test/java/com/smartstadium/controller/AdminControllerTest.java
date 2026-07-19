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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;

    private String adminToken;
    private String userToken;

    @BeforeEach
    void setUp() {
        adminToken = jwtService.generateAccessToken("ayushgdg18@gmail.com", "ADMIN");
        userToken = jwtService.generateAccessToken("regularuser@test.com", "USER");

        // Ensure admin user exists
        if (userRepository.findByEmail("ayushgdg18@gmail.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("ayushgdg18@gmail.com")
                    .name("Admin")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.ADMIN)
                    .build());
        }
        // Ensure regular user exists
        if (userRepository.findByEmail("regularuser@test.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("regularuser@test.com")
                    .name("Regular User")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.USER)
                    .build());
        }
        // Ensure a volunteer user exists
        if (userRepository.findByEmail("volunteer@test.com").isEmpty()) {
            userRepository.save(User.builder()
                    .email("volunteer@test.com")
                    .name("Volunteer User")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.VOLUNTEER)
                    .build());
        }
    }

    // ── GET /api/admin/users ──────────────────────────────────

    @Nested
    @DisplayName("GET /api/admin/users")
    class ListUsers {

        @Test
        @DisplayName("should return 200 with user list for admin")
        void adminCanListUsers() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                    .andExpect(jsonPath("$[0].email", notNullValue()))
                    .andExpect(jsonPath("$[0].role", notNullValue()));
        }

        @Test
        @DisplayName("should return 403 for non-admin user")
        void nonAdminCannotListUsers() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.message").value("Access denied"));
        }

        @Test
        @DisplayName("should return 401 without authorization header")
        void noAuthReturns401() throws Exception {
            mockMvc.perform(get("/api/admin/users"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should include all required fields for each user")
        void userListContainsRequiredFields() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id", notNullValue()))
                    .andExpect(jsonPath("$[0].email", notNullValue()))
                    .andExpect(jsonPath("$[0].role", notNullValue()))
                    .andExpect(jsonPath("$[0].authProvider", notNullValue()))
                    .andExpect(jsonPath("$[0].createdAt", notNullValue()));
        }

        @Test
        @DisplayName("should return correct role values (USER, VOLUNTEER, ADMIN)")
        void rolesAreValidEnumValues() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[*].role",
                            everyItem(isIn(new String[]{"USER", "VOLUNTEER", "ADMIN"}))));
        }

        @Test
        @DisplayName("should return admin user with ADMIN role")
        void adminUserHasAdminRole() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[?(@.email=='ayushgdg18@gmail.com')].role",
                            hasItem("ADMIN")));
        }

        @Test
        @DisplayName("should reject request with invalid JWT")
        void invalidJwtReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer invalid.jwt.token"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("should reject request with expired JWT")
        void expiredJwtReturnsUnauthorized() throws Exception {
            JwtService shortLived = new JwtService(
                    "SmartStadium2026SecretKeyThatIsAtLeast256BitsLongForHS256Algorithm!", 0, 0);
            String expiredToken = shortLived.generateAccessToken("ayushgdg18@gmail.com", "ADMIN");

            mockMvc.perform(get("/api/admin/users")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── PUT /api/admin/users/{id}/role ────────────────────────

    @Nested
    @DisplayName("PUT /api/admin/users/{id}/role")
    class UpdateRole {

        @Test
        @DisplayName("should update user role to VOLUNTEER")
        void updateRoleToVolunteer() throws Exception {
            User user = userRepository.findByEmail("regularuser@test.com").orElseThrow();

            mockMvc.perform(put("/api/admin/users/" + user.getId() + "/role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"VOLUNTEER\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("VOLUNTEER"))
                    .andExpect(jsonPath("$.email").value("regularuser@test.com"));

            // Reset for other tests
            user.setRole(UserRole.USER);
            userRepository.save(user);
        }

        @Test
        @DisplayName("should update user role back to USER (revoke)")
        void revokeVolunteerRole() throws Exception {
            User user = userRepository.findByEmail("volunteer@test.com").orElseThrow();

            mockMvc.perform(put("/api/admin/users/" + user.getId() + "/role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"USER\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("USER"));

            // Reset
            user.setRole(UserRole.VOLUNTEER);
            userRepository.save(user);
        }

        @Test
        @DisplayName("should return 400 for invalid role name")
        void invalidRoleName() throws Exception {
            User user = userRepository.findByEmail("regularuser@test.com").orElseThrow();

            mockMvc.perform(put("/api/admin/users/" + user.getId() + "/role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"SUPERADMIN\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Invalid role")));
        }

        @Test
        @DisplayName("should return 400 when role field is missing")
        void missingRoleField() throws Exception {
            User user = userRepository.findByEmail("regularuser@test.com").orElseThrow();

            mockMvc.perform(put("/api/admin/users/" + user.getId() + "/role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"other\":\"value\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Role is required"));
        }

        @Test
        @DisplayName("should return 404 for non-existent user ID")
        void nonExistentUserId() throws Exception {
            mockMvc.perform(put("/api/admin/users/99999/role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"VOLUNTEER\"}"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should prevent changing admin's own role")
        void cannotChangeAdminRole() throws Exception {
            User admin = userRepository.findByEmail("ayushgdg18@gmail.com").orElseThrow();

            mockMvc.perform(put("/api/admin/users/" + admin.getId() + "/role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"USER\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("Cannot change admin")));
        }

        @Test
        @DisplayName("should return 403 when non-admin tries to update role")
        void nonAdminCannotUpdateRole() throws Exception {
            User user = userRepository.findByEmail("regularuser@test.com").orElseThrow();

            mockMvc.perform(put("/api/admin/users/" + user.getId() + "/role")
                            .header("Authorization", "Bearer " + userToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"ADMIN\"}"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should accept role name case-insensitively")
        void roleNameCaseInsensitive() throws Exception {
            User user = userRepository.findByEmail("regularuser@test.com").orElseThrow();

            mockMvc.perform(put("/api/admin/users/" + user.getId() + "/role")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"role\":\"volunteer\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.role").value("VOLUNTEER"));

            // Reset
            user.setRole(UserRole.USER);
            userRepository.save(user);
        }
    }

    // ── GET /api/admin/stats ─────────────────────────────────

    @Nested
    @DisplayName("GET /api/admin/stats")
    class Stats {

        @Test
        @DisplayName("should return stats with correct structure")
        void statsHaveCorrectStructure() throws Exception {
            mockMvc.perform(get("/api/admin/stats")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$.volunteers", greaterThanOrEqualTo(0)))
                    .andExpect(jsonPath("$.admins", greaterThanOrEqualTo(1)))
                    .andExpect(jsonPath("$.regularUsers", greaterThanOrEqualTo(0)));
        }

        @Test
        @DisplayName("should return 403 for non-admin")
        void nonAdminCannotViewStats() throws Exception {
            mockMvc.perform(get("/api/admin/stats")
                            .header("Authorization", "Bearer " + userToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("totalUsers should equal sum of all role counts")
        void totalUsersSumsCorrectly() throws Exception {
            String response = mockMvc.perform(get("/api/admin/stats")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();

            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var stats = mapper.readTree(response);
            long total = stats.get("totalUsers").asLong();
            long sum = stats.get("volunteers").asLong()
                    + stats.get("admins").asLong()
                    + stats.get("regularUsers").asLong();

            org.assertj.core.api.Assertions.assertThat(total).isEqualTo(sum);
        }
    }
}
