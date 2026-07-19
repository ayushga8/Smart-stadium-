package com.smartstadium.controller;

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
@DisplayName("Stadium Controller")
class StadiumControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private JwtService jwtService;

    private String token;

    @BeforeEach
    void setUp() {
        token = jwtService.generateAccessToken("test@example.com", "USER");
    }

    @Nested
    @DisplayName("GET /api/stadium/map")
    class GetMap {

        @Test
        @DisplayName("returns map data with zones and amenities")
        void returnsMapData() throws Exception {
            mockMvc.perform(get("/api/stadium/map")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.zones").isArray())
                .andExpect(jsonPath("$.amenities").isArray());
        }

        @Test
        @DisplayName("zones have required fields")
        void zonesHaveFields() throws Exception {
            mockMvc.perform(get("/api/stadium/map")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.zones[0].id").exists())
                .andExpect(jsonPath("$.zones[0].name").exists())
                .andExpect(jsonPath("$.zones[0].type").exists())
                .andExpect(jsonPath("$.zones[0].capacity").exists())
                .andExpect(jsonPath("$.zones[0].level").exists());
        }

        @Test
        @DisplayName("amenities have required fields")
        void amenitiesHaveFields() throws Exception {
            mockMvc.perform(get("/api/stadium/map")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.amenities[0].id").exists())
                .andExpect(jsonPath("$.amenities[0].name").exists())
                .andExpect(jsonPath("$.amenities[0].type").exists())
                .andExpect(jsonPath("$.amenities[0].zone").exists())
                .andExpect(jsonPath("$.amenities[0].description").exists());
        }

        @Test
        @DisplayName("has multiple zones")
        void hasMultipleZones() throws Exception {
            mockMvc.perform(get("/api/stadium/map")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.zones", hasSize(greaterThanOrEqualTo(10))));
        }

        @Test
        @DisplayName("has multiple amenities")
        void hasMultipleAmenities() throws Exception {
            mockMvc.perform(get("/api/stadium/map")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.amenities", hasSize(greaterThanOrEqualTo(10))));
        }

        @Test
        @DisplayName("returns 401 without token")
        void returns401() throws Exception {
            mockMvc.perform(get("/api/stadium/map"))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/stadium/crowd")
    class GetCrowd {

        @Test
        @DisplayName("returns crowd density data")
        void returnsCrowdData() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd")
                    .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(10))));
        }

        @Test
        @DisplayName("crowd data has zone id")
        void hasZoneId() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].zoneId").exists());
        }

        @Test
        @DisplayName("crowd data has zone name")
        void hasZoneName() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].zoneName").exists());
        }

        @Test
        @DisplayName("crowd data has capacity")
        void hasCapacity() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].capacity").isNumber());
        }

        @Test
        @DisplayName("crowd data has occupancy")
        void hasOccupancy() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].occupancy").isNumber());
        }

        @Test
        @DisplayName("crowd data has percentage")
        void hasPercentage() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].percentage").isNumber());
        }

        @Test
        @DisplayName("crowd data has status")
        void hasStatus() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd")
                    .header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$[0].status").exists());
        }

        @Test
        @DisplayName("returns 401 without token")
        void returns401() throws Exception {
            mockMvc.perform(get("/api/stadium/crowd"))
                .andExpect(status().isUnauthorized());
        }
    }
}
