package com.smartstadium.controller;

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
@DisplayName("Match Controller")
class MatchControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtService.generateAccessToken("test@example.com", "USER");
    }

    @Nested
    @DisplayName("GET /api/matches")
    class GetAllMatches {

        @Test
        @DisplayName("returns match list with valid token")
        void returnsMatchList() throws Exception {
            mockMvc.perform(get("/api/matches")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))));
        }

        @Test
        @DisplayName("each match has id field")
        void matchesHaveId() throws Exception {
            mockMvc.perform(get("/api/matches")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].id").exists());
        }

        @Test
        @DisplayName("each match has team fields")
        void matchesHaveTeams() throws Exception {
            mockMvc.perform(get("/api/matches")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].teamA").exists())
                .andExpect(jsonPath("$[0].teamB").exists());
        }

        @Test
        @DisplayName("each match has venue")
        void matchesHaveVenue() throws Exception {
            mockMvc.perform(get("/api/matches")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].venue").exists());
        }

        @Test
        @DisplayName("each match has kickoff time")
        void matchesHaveKickoff() throws Exception {
            mockMvc.perform(get("/api/matches")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].kickoff").exists());
        }

        @Test
        @DisplayName("each match has stage")
        void matchesHaveStage() throws Exception {
            mockMvc.perform(get("/api/matches")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].stage").exists());
        }

        @Test
        @DisplayName("each match has flag emojis")
        void matchesHaveFlags() throws Exception {
            mockMvc.perform(get("/api/matches")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].flagA").exists())
                .andExpect(jsonPath("$[0].flagB").exists());
        }

        @Test
        @DisplayName("returns 401 without token")
        void returns401WithoutToken() throws Exception {
            mockMvc.perform(get("/api/matches"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/matches/{id}")
    class GetMatchById {

        @Test
        @DisplayName("returns match for valid id")
        void returnsMatch() throws Exception {
            mockMvc.perform(get("/api/matches/m1")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamA").value("USA"))
                .andExpect(jsonPath("$.teamB").value("Morocco"));
        }

        @Test
        @DisplayName("returns 404 for invalid id")
        void returns404() throws Exception {
            mockMvc.perform(get("/api/matches/nonexistent")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("returns final match")
        void returnsFinal() throws Exception {
            mockMvc.perform(get("/api/matches/final")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stage").value("Final"));
        }

        @Test
        @DisplayName("returns quarter-final match")
        void returnsQuarterFinal() throws Exception {
            mockMvc.perform(get("/api/matches/qf1")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stage").value("Quarter-Final"));
        }

        @Test
        @DisplayName("match has all required fields")
        void matchHasAllFields() throws Exception {
            mockMvc.perform(get("/api/matches/m1")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.id").value("m1"))
                .andExpect(jsonPath("$.venue").exists())
                .andExpect(jsonPath("$.kickoff").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.group").exists());
        }

        @Test
        @DisplayName("returns 401 without token")
        void returns401() throws Exception {
            mockMvc.perform(get("/api/matches/m1"))
                .andExpect(status().isUnauthorized());
        }
    }
}
