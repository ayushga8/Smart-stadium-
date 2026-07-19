package com.smartstadium.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Match Service — edge cases")
class MatchServiceEdgeCaseTest {

    private final MatchService matchService = new MatchService();

    @ParameterizedTest
    @DisplayName("all known match IDs are retrievable")
    @ValueSource(strings = {"m1", "m2", "m3", "m4", "m5", "m6", "m7", "m8", "m9", "m10", "qf1", "sf1", "final"})
    void allKnownIdsRetrievable(String id) {
        assertTrue(matchService.getMatchById(id).isPresent(), "Match " + id + " should exist");
    }

    @Test
    @DisplayName("match IDs are unique")
    void matchIdsUnique() {
        List<Map<String, Object>> matches = matchService.getAllMatches();
        long uniqueIds = matches.stream().map(m -> m.get("id")).distinct().count();
        assertEquals(matches.size(), uniqueIds);
    }

    @Test
    @DisplayName("getMatchesByVenue returns subset of all matches")
    void venueFilterIsSubset() {
        List<Map<String, Object>> all = matchService.getAllMatches();
        List<Map<String, Object>> filtered = matchService.getMatchesByVenue("MetLife");
        assertTrue(filtered.size() <= all.size());
    }

    @Test
    @DisplayName("getMatchesByVenue with empty string returns all")
    void emptyVenueReturnsAll() {
        List<Map<String, Object>> result = matchService.getMatchesByVenue("");
        assertEquals(matchService.getAllMatches().size(), result.size());
    }

    @Test
    @DisplayName("all stages are valid FIFA stages")
    void validStages() {
        List<String> validStages = List.of("Group Stage", "Quarter-Final", "Semi-Final", "Third Place", "Final");
        matchService.getAllMatches().forEach(m -> {
            String stage = (String) m.get("stage");
            assertTrue(validStages.contains(stage), "Invalid stage: " + stage);
        });
    }

    @Test
    @DisplayName("context summary is non-empty")
    void contextSummaryNonEmpty() {
        String summary = matchService.getContextSummary();
        assertFalse(summary.isBlank());
        assertTrue(summary.length() > 100);
    }

    @Test
    @DisplayName("getAllMatches returns same count on repeated calls")
    void allMatchesIdempotent() {
        List<Map<String, Object>> first = matchService.getAllMatches();
        List<Map<String, Object>> second = matchService.getAllMatches();
        assertEquals(first.size(), second.size());
    }

    @Test
    @DisplayName("total matches count is 13")
    void totalMatchCount() {
        assertEquals(13, matchService.getAllMatches().size());
    }
}
