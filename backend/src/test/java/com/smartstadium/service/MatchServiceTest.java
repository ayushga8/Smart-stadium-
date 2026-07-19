package com.smartstadium.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MatchServiceTest {

    private final MatchService matchService = new MatchService();

    // === getAllMatches ===

    @Test
    @DisplayName("getAllMatches returns all matches")
    void getAllMatchesReturnsAll() {
        List<Map<String, Object>> matches = matchService.getAllMatches();
        assertFalse(matches.isEmpty(), "Match list should not be empty");
        assertTrue(matches.size() >= 10, "Should have at least 10 matches");
    }

    @Test
    @DisplayName("Each match has required fields")
    void matchesHaveRequiredFields() {
        List<Map<String, Object>> matches = matchService.getAllMatches();
        for (Map<String, Object> match : matches) {
            assertNotNull(match.get("id"), "Match should have id");
            assertNotNull(match.get("teamA"), "Match should have teamA");
            assertNotNull(match.get("teamB"), "Match should have teamB");
            assertNotNull(match.get("venue"), "Match should have venue");
            assertNotNull(match.get("kickoff"), "Match should have kickoff");
            assertNotNull(match.get("stage"), "Match should have stage");
            assertNotNull(match.get("status"), "Match should have status");
        }
    }

    @Test
    @DisplayName("All matches have upcoming status")
    void allMatchesUpcoming() {
        matchService.getAllMatches().forEach(m ->
            assertEquals("upcoming", m.get("status"))
        );
    }

    // === getMatchById ===

    @Test
    @DisplayName("getMatchById returns match for valid ID")
    void getMatchByIdValid() {
        Optional<Map<String, Object>> match = matchService.getMatchById("m1");
        assertTrue(match.isPresent());
        assertEquals("USA", match.get().get("teamA"));
        assertEquals("Morocco", match.get().get("teamB"));
    }

    @Test
    @DisplayName("getMatchById returns empty for invalid ID")
    void getMatchByIdInvalid() {
        Optional<Map<String, Object>> match = matchService.getMatchById("nonexistent");
        assertFalse(match.isPresent());
    }

    @Test
    @DisplayName("getMatchById returns final match")
    void getMatchByIdFinal() {
        Optional<Map<String, Object>> match = matchService.getMatchById("final");
        assertTrue(match.isPresent());
        assertEquals("Final", match.get().get("stage"));
    }

    // === getMatchesByVenue ===

    @Test
    @DisplayName("getMatchesByVenue filters MetLife matches")
    void getMatchesByVenueMetLife() {
        List<Map<String, Object>> metLife = matchService.getMatchesByVenue("MetLife");
        assertFalse(metLife.isEmpty());
        metLife.forEach(m ->
            assertTrue(((String) m.get("venue")).contains("MetLife"))
        );
    }

    @Test
    @DisplayName("getMatchesByVenue is case-insensitive")
    void getMatchesByVenueCaseInsensitive() {
        List<Map<String, Object>> upper = matchService.getMatchesByVenue("METLIFE");
        List<Map<String, Object>> lower = matchService.getMatchesByVenue("metlife");
        assertEquals(upper.size(), lower.size());
    }

    @Test
    @DisplayName("getMatchesByVenue returns empty for unknown venue")
    void getMatchesByVenueUnknown() {
        List<Map<String, Object>> result = matchService.getMatchesByVenue("Wembley");
        assertTrue(result.isEmpty());
    }

    // === getContextSummary ===

    @Test
    @DisplayName("getContextSummary contains FIFA header")
    void contextSummaryHasHeader() {
        String summary = matchService.getContextSummary();
        assertTrue(summary.contains("FIFA WORLD CUP 2026"));
    }

    @Test
    @DisplayName("getContextSummary contains all team names")
    void contextSummaryContainsTeams() {
        String summary = matchService.getContextSummary();
        assertTrue(summary.contains("USA"));
        assertTrue(summary.contains("Germany"));
        assertTrue(summary.contains("Brazil"));
        assertTrue(summary.contains("France"));
    }
}
