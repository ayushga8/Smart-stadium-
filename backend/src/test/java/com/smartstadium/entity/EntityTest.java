package com.smartstadium.entity;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Entity model tests")
class EntityTest {

    @Nested
    @DisplayName("UserRole enum")
    class UserRoleTest {

        @Test
        @DisplayName("should have exactly 3 roles")
        void hasThreeRoles() {
            assertThat(UserRole.values()).hasSize(3);
        }

        @Test
        @DisplayName("should contain USER role")
        void hasUserRole() {
            assertThat(UserRole.valueOf("USER")).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("should contain VOLUNTEER role")
        void hasVolunteerRole() {
            assertThat(UserRole.valueOf("VOLUNTEER")).isEqualTo(UserRole.VOLUNTEER);
        }

        @Test
        @DisplayName("should contain ADMIN role")
        void hasAdminRole() {
            assertThat(UserRole.valueOf("ADMIN")).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("should throw for invalid role name")
        void invalidRoleThrows() {
            assertThatThrownBy(() -> UserRole.valueOf("SUPERADMIN"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("AuthProvider enum")
    class AuthProviderTest {

        @Test
        @DisplayName("should have exactly 2 providers")
        void hasTwoProviders() {
            assertThat(AuthProvider.values()).hasSize(2);
        }

        @Test
        @DisplayName("should contain LOCAL provider")
        void hasLocalProvider() {
            assertThat(AuthProvider.valueOf("LOCAL")).isEqualTo(AuthProvider.LOCAL);
        }

        @Test
        @DisplayName("should contain GOOGLE provider")
        void hasGoogleProvider() {
            assertThat(AuthProvider.valueOf("GOOGLE")).isEqualTo(AuthProvider.GOOGLE);
        }
    }

    @Nested
    @DisplayName("User entity")
    class UserEntityTest {

        @Test
        @DisplayName("should default role to USER via @Builder.Default")
        void defaultRoleIsUser() {
            User user = User.builder()
                    .email("test@test.com")
                    .authProvider(AuthProvider.LOCAL)
                    .build();
            assertThat(user.getRole()).isEqualTo(UserRole.USER);
        }

        @Test
        @DisplayName("should allow setting role to VOLUNTEER")
        void canSetVolunteerRole() {
            User user = User.builder()
                    .email("test@test.com")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.VOLUNTEER)
                    .build();
            assertThat(user.getRole()).isEqualTo(UserRole.VOLUNTEER);
        }

        @Test
        @DisplayName("should allow setting role to ADMIN")
        void canSetAdminRole() {
            User user = User.builder()
                    .email("test@test.com")
                    .authProvider(AuthProvider.LOCAL)
                    .role(UserRole.ADMIN)
                    .build();
            assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        }

        @Test
        @DisplayName("should store email correctly")
        void storesEmail() {
            User user = User.builder().email("hello@test.com").authProvider(AuthProvider.LOCAL).build();
            assertThat(user.getEmail()).isEqualTo("hello@test.com");
        }

        @Test
        @DisplayName("should store name correctly")
        void storesName() {
            User user = User.builder().email("a@b.com").name("Test Name").authProvider(AuthProvider.LOCAL).build();
            assertThat(user.getName()).isEqualTo("Test Name");
        }

        @Test
        @DisplayName("should allow null name")
        void allowsNullName() {
            User user = User.builder().email("a@b.com").authProvider(AuthProvider.LOCAL).build();
            assertThat(user.getName()).isNull();
        }

        @Test
        @DisplayName("should set lastLoginAt via setter")
        void setsLastLoginAt() {
            User user = User.builder().email("a@b.com").authProvider(AuthProvider.LOCAL).build();
            LocalDateTime now = LocalDateTime.now();
            user.setLastLoginAt(now);
            assertThat(user.getLastLoginAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("should allow switching auth provider")
        void canSwitchAuthProvider() {
            User user = User.builder().email("a@b.com").authProvider(AuthProvider.LOCAL).build();
            user.setAuthProvider(AuthProvider.GOOGLE);
            assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        }

        @Test
        @DisplayName("should allow role change via setter")
        void canChangeRoleViaSetter() {
            User user = User.builder().email("a@b.com").authProvider(AuthProvider.LOCAL).build();
            assertThat(user.getRole()).isEqualTo(UserRole.USER);

            user.setRole(UserRole.VOLUNTEER);
            assertThat(user.getRole()).isEqualTo(UserRole.VOLUNTEER);

            user.setRole(UserRole.ADMIN);
            assertThat(user.getRole()).isEqualTo(UserRole.ADMIN);
        }
    }

    @Nested
    @DisplayName("AuthResponseDto")
    class AuthResponseDtoTest {

        @Test
        @DisplayName("should include role field in response")
        void hasRoleField() {
            var dto = com.smartstadium.dto.AuthResponseDto.builder()
                    .accessToken("token")
                    .email("test@test.com")
                    .name("Test")
                    .role("ADMIN")
                    .build();
            assertThat(dto.getRole()).isEqualTo("ADMIN");
        }

        @Test
        @DisplayName("should include all fields")
        void hasAllFields() {
            var dto = com.smartstadium.dto.AuthResponseDto.builder()
                    .accessToken("mytoken")
                    .email("e@e.com")
                    .name("Name")
                    .role("USER")
                    .build();
            assertThat(dto.getAccessToken()).isEqualTo("mytoken");
            assertThat(dto.getEmail()).isEqualTo("e@e.com");
            assertThat(dto.getName()).isEqualTo("Name");
            assertThat(dto.getRole()).isEqualTo("USER");
        }
    }
}
