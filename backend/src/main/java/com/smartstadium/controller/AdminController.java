package com.smartstadium.controller;

import com.smartstadium.entity.User;
import com.smartstadium.entity.UserRole;
import com.smartstadium.repository.UserRepository;
import com.smartstadium.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final String ADMIN_EMAIL = "ayushgdg18@gmail.com";

    private final JwtService jwtService;
    private final UserRepository userRepository;

    /** List all users (admin only) */
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(@RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
        }

        List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(u -> Map.<String, Object>of(
                        "id", u.getId(),
                        "email", u.getEmail(),
                        "name", u.getName() != null ? u.getName() : "",
                        "role", u.getRole().name(),
                        "authProvider", u.getAuthProvider().name(),
                        "createdAt", u.getCreatedAt().toString(),
                        "lastLoginAt", u.getLastLoginAt().toString()
                ))
                .toList();

        return ResponseEntity.ok(users);
    }

    /** Update a user's role (admin only) */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long userId,
            @RequestBody Map<String, String> body) {

        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
        }

        String newRole = body.get("role");
        if (newRole == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Role is required"));
        }

        UserRole role;
        try {
            role = UserRole.valueOf(newRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Invalid role. Must be USER, VOLUNTEER, or ADMIN"));
        }

        return userRepository.findById(userId)
                .map(user -> {
                    // Prevent changing admin's own role
                    if (ADMIN_EMAIL.equalsIgnoreCase(user.getEmail())) {
                        return ResponseEntity.badRequest()
                                .body((Object) Map.of("message", "Cannot change admin role"));
                    }
                    user.setRole(role);
                    userRepository.save(user);
                    return ResponseEntity.ok(
                            (Object) Map.of("message", "Role updated", "email", user.getEmail(), "role", role.name()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /** Admin dashboard stats */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats(@RequestHeader("Authorization") String authHeader) {
        if (!isAdmin(authHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Access denied"));
        }

        long totalUsers = userRepository.count();
        long volunteers = userRepository.countByRole(UserRole.VOLUNTEER);
        long admins = userRepository.countByRole(UserRole.ADMIN);

        return ResponseEntity.ok(Map.of(
                "totalUsers", totalUsers,
                "volunteers", volunteers,
                "admins", admins,
                "regularUsers", totalUsers - volunteers - admins
        ));
    }

    private boolean isAdmin(String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            String email = jwtService.extractEmail(token);
            return ADMIN_EMAIL.equalsIgnoreCase(email);
        } catch (Exception e) {
            return false;
        }
    }
}
