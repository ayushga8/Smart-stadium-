package com.smartstadium.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MatchService {

    public record Match(String id, String teamA, String teamB, String flagA, String flagB,
                        String group, String venue, LocalDateTime kickoff, String stage, String status) {}

    private static final List<Match> MATCHES = List.of(
        // Group Stage - Day 1
        new Match("m1", "USA", "Morocco", "🇺🇸", "🇲🇦",
            "Group A", "MetLife Stadium", LocalDateTime.of(2026, 6, 11, 17, 0), "Group Stage", "upcoming"),
        new Match("m2", "Mexico", "Canada", "🇲🇽", "🇨🇦",
            "Group A", "Rose Bowl", LocalDateTime.of(2026, 6, 11, 20, 0), "Group Stage", "upcoming"),
        new Match("m3", "Brazil", "Serbia", "🇧🇷", "🇷🇸",
            "Group B", "SoFi Stadium", LocalDateTime.of(2026, 6, 12, 14, 0), "Group Stage", "upcoming"),
        new Match("m4", "Germany", "Japan", "🇩🇪", "🇯🇵",
            "Group B", "MetLife Stadium", LocalDateTime.of(2026, 6, 12, 20, 0), "Group Stage", "upcoming"),
        new Match("m5", "Argentina", "Australia", "🇦🇷", "🇦🇺",
            "Group C", "Hard Rock Stadium", LocalDateTime.of(2026, 6, 13, 17, 0), "Group Stage", "upcoming"),
        new Match("m6", "France", "South Korea", "🇫🇷", "🇰🇷",
            "Group C", "MetLife Stadium", LocalDateTime.of(2026, 6, 13, 20, 0), "Group Stage", "upcoming"),
        new Match("m7", "England", "Nigeria", "🏴󠁧󠁢󠁥󠁮󠁧󠁿", "🇳🇬",
            "Group D", "AT&T Stadium", LocalDateTime.of(2026, 6, 14, 14, 0), "Group Stage", "upcoming"),
        new Match("m8", "Spain", "Chile", "🇪🇸", "🇨🇱",
            "Group D", "Levi's Stadium", LocalDateTime.of(2026, 6, 14, 17, 0), "Group Stage", "upcoming"),
        new Match("m9", "Netherlands", "Ecuador", "🇳🇱", "🇪🇨",
            "Group E", "MetLife Stadium", LocalDateTime.of(2026, 6, 15, 14, 0), "Group Stage", "upcoming"),
        new Match("m10", "Portugal", "Ghana", "🇵🇹", "🇬🇭",
            "Group E", "Lincoln Financial Field", LocalDateTime.of(2026, 6, 15, 20, 0), "Group Stage", "upcoming"),

        // Knockout
        new Match("qf1", "TBD", "TBD", "🏳️", "🏳️",
            null, "MetLife Stadium", LocalDateTime.of(2026, 7, 10, 18, 0), "Quarter-Final", "upcoming"),
        new Match("sf1", "TBD", "TBD", "🏳️", "🏳️",
            null, "MetLife Stadium", LocalDateTime.of(2026, 7, 14, 20, 0), "Semi-Final", "upcoming"),
        new Match("final", "TBD", "TBD", "🏳️", "🏳️",
            null, "MetLife Stadium", LocalDateTime.of(2026, 7, 19, 16, 0), "Final", "upcoming")
    );

    public List<Map<String, Object>> getAllMatches() {
        return MATCHES.stream().map(this::toMap).toList();
    }

    public List<Map<String, Object>> getMatchesByVenue(String venue) {
        return MATCHES.stream()
            .filter(m -> m.venue().toLowerCase().contains(venue.toLowerCase()))
            .map(this::toMap).toList();
    }

    public Optional<Map<String, Object>> getMatchById(String id) {
        return MATCHES.stream().filter(m -> m.id().equals(id)).findFirst().map(this::toMap);
    }

    /** Returns a text summary for the AI to use as context */
    public String getContextSummary() {
        StringBuilder sb = new StringBuilder("FIFA WORLD CUP 2026 MATCH SCHEDULE:\n\n");
        MATCHES.forEach(m -> {
            sb.append("- ").append(m.flagA()).append(" ").append(m.teamA())
              .append(" vs ").append(m.teamB()).append(" ").append(m.flagB())
              .append(" | ").append(m.stage());
            if (m.group() != null) sb.append(" (").append(m.group()).append(")");
            sb.append(" | ").append(m.venue())
              .append(" | ").append(m.kickoff()).append("\n");
        });
        return sb.toString();
    }

    private Map<String, Object> toMap(Match m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.id());
        map.put("teamA", m.teamA());
        map.put("teamB", m.teamB());
        map.put("flagA", m.flagA());
        map.put("flagB", m.flagB());
        map.put("group", m.group());
        map.put("venue", m.venue());
        map.put("kickoff", m.kickoff());
        map.put("stage", m.stage());
        map.put("status", m.status());
        return map;
    }
}
