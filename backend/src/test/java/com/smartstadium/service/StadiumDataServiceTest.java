package com.smartstadium.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class StadiumDataServiceTest {

    private final StadiumDataService service = new StadiumDataService();

    // === getMapData ===

    @Test
    @DisplayName("getMapData returns all zones")
    void getMapDataReturnsZones() {
        List<Map<String, Object>> zones = service.getMapData();
        assertFalse(zones.isEmpty());
        assertTrue(zones.size() >= 10, "Should have at least 10 zones");
    }

    @Test
    @DisplayName("Each zone has required fields")
    void zonesHaveRequiredFields() {
        service.getMapData().forEach(zone -> {
            assertNotNull(zone.get("id"));
            assertNotNull(zone.get("name"));
            assertNotNull(zone.get("type"));
            assertNotNull(zone.get("capacity"));
            assertNotNull(zone.get("level"));
        });
    }

    @Test
    @DisplayName("Zone types include seating, food, concourse, gate, vip")
    void zoneTypesAreValid() {
        List<String> validTypes = List.of("seating", "food", "concourse", "gate", "vip");
        service.getMapData().forEach(zone -> {
            String type = (String) zone.get("type");
            assertTrue(validTypes.contains(type), "Invalid zone type: " + type);
        });
    }

    @Test
    @DisplayName("Zone capacities are positive")
    void zoneCapacitiesPositive() {
        service.getMapData().forEach(zone -> {
            int capacity = (int) zone.get("capacity");
            assertTrue(capacity > 0, "Capacity must be positive");
        });
    }

    // === getAmenities ===

    @Test
    @DisplayName("getAmenities returns amenities")
    void getAmenitiesReturnsData() {
        List<Map<String, Object>> amenities = service.getAmenities();
        assertFalse(amenities.isEmpty());
        assertTrue(amenities.size() >= 10, "Should have at least 10 amenities");
    }

    @Test
    @DisplayName("Each amenity has required fields")
    void amenitiesHaveRequiredFields() {
        service.getAmenities().forEach(a -> {
            assertNotNull(a.get("id"));
            assertNotNull(a.get("name"));
            assertNotNull(a.get("type"));
            assertNotNull(a.get("zone"));
            assertNotNull(a.get("description"));
        });
    }

    @Test
    @DisplayName("Amenity types include restroom, medical, food, accessibility")
    void amenityTypesIncludeEssentials() {
        List<String> types = service.getAmenities().stream()
            .map(a -> (String) a.get("type"))
            .distinct()
            .toList();
        assertTrue(types.contains("restroom"), "Should have restroom amenities");
        assertTrue(types.contains("medical"), "Should have medical amenities");
        assertTrue(types.contains("food"), "Should have food amenities");
        assertTrue(types.contains("accessibility"), "Should have accessibility amenities");
    }

    // === getCrowdDensity ===

    @Test
    @DisplayName("getCrowdDensity returns data for all zones")
    void crowdDensityReturnsAllZones() {
        List<StadiumDataService.CrowdData> crowd = service.getCrowdDensity();
        assertEquals(service.getMapData().size(), crowd.size());
    }

    @Test
    @DisplayName("Crowd occupancy does not exceed capacity")
    void crowdOccupancyWithinCapacity() {
        service.getCrowdDensity().forEach(c -> {
            assertTrue(c.occupancy() <= c.capacity(),
                c.zoneName() + " occupancy exceeds capacity");
        });
    }

    @Test
    @DisplayName("Crowd percentage is between 0 and 1")
    void crowdPercentageInRange() {
        service.getCrowdDensity().forEach(c -> {
            assertTrue(c.percentage() >= 0.0 && c.percentage() <= 1.0,
                c.zoneName() + " percentage out of range: " + c.percentage());
        });
    }

    @Test
    @DisplayName("Crowd status is low, moderate, or high")
    void crowdStatusValid() {
        List<String> validStatuses = List.of("low", "moderate", "high");
        service.getCrowdDensity().forEach(c -> {
            assertTrue(validStatuses.contains(c.status()),
                "Invalid status: " + c.status());
        });
    }

    @Test
    @DisplayName("High status corresponds to percentage > 0.80")
    void highStatusMeansHighPercentage() {
        service.getCrowdDensity().stream()
            .filter(c -> c.status().equals("high"))
            .forEach(c -> assertTrue(c.percentage() > 0.80,
                c.zoneName() + " is 'high' but percentage is " + c.percentage()));
    }

    // === getContextSummary ===

    @Test
    @DisplayName("getContextSummary contains stadium info")
    void contextSummaryHasStadiumInfo() {
        String summary = service.getContextSummary();
        assertTrue(summary.contains("MetLife Stadium"));
        assertTrue(summary.contains("82,500"));
    }

    @Test
    @DisplayName("getContextSummary contains zones and amenities sections")
    void contextSummaryHasSections() {
        String summary = service.getContextSummary();
        assertTrue(summary.contains("ZONES:"));
        assertTrue(summary.contains("AMENITIES:"));
        assertTrue(summary.contains("CURRENT CROWD:"));
    }
}
