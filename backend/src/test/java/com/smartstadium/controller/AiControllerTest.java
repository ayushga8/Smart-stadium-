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
@DisplayName("AI Controller")
class AiControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;
    @Autowired private UserRepository userRepository;

    private String token;

    @BeforeEach
    void setUp() {
        String email = "aitest@example.com";
        if (userRepository.findByEmail(email).isEmpty()) {
            userRepository.save(User.builder()
                .email(email).name("AI Tester")
                .authProvider(AuthProvider.LOCAL).role(UserRole.USER).build());
        }
        token = jwtService.generateAccessToken(email, "USER");
    }

    @Nested
    @DisplayName("POST /api/ai/chat")
    class Chat {

        @Test
        @DisplayName("returns response for hello message")
        void chatHello() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Hello\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists())
                .andExpect(jsonPath("$.language").value("en"))
                .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("returns food info for food query")
        void chatFood() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Where can I eat?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("Food")));
        }

        @Test
        @DisplayName("returns gate info for gate query")
        void chatGate() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Where is gate A?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("Gate")));
        }

        @Test
        @DisplayName("returns parking info for parking query")
        void chatParking() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Where is parking?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("Park")));
        }

        @Test
        @DisplayName("returns emergency info")
        void chatEmergency() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"I need medical help\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("Emergency")));
        }

        @Test
        @DisplayName("returns accessibility info")
        void chatAccessibility() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Where is the elevator?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
        }

        @Test
        @DisplayName("returns match info")
        void chatMatch() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"When is the next game?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").exists());
        }

        @Test
        @DisplayName("returns wifi info")
        void chatWifi() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Is there wifi?\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("WiFi")));
        }

        @Test
        @DisplayName("returns 401 without token")
        void returns401() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Hello\"}"))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("returns default help for unknown query")
        void chatUnknown() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"random xyz 12345\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response", containsString("Stadium Assistant")));
        }
    }

    @Nested
    @DisplayName("GET /api/ai/history")
    class History {

        @Test
        @DisplayName("returns chat history")
        void returnsHistory() throws Exception {
            // Send a message first
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Hello history test\"}"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/ai/history")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("history entry has required fields")
        void historyHasFields() throws Exception {
            mockMvc.perform(post("/api/ai/chat")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"message\": \"Test fields\"}"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/ai/history")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].userMessage").exists())
                .andExpect(jsonPath("$[0].aiResponse").exists())
                .andExpect(jsonPath("$[0].language").exists())
                .andExpect(jsonPath("$[0].timestamp").exists());
        }

        @Test
        @DisplayName("returns 401 without token")
        void returns401() throws Exception {
            mockMvc.perform(get("/api/ai/history"))
                .andExpect(status().isUnauthorized());
        }
    }
}
