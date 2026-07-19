package com.smartstadium.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class StadiumDataService {

    public record Zone(String id, String name, String type, int capacity, int currentOccupancy, String level) {}
    public record Amenity(String id, String name, String type, String zone, String description) {}
    public record CrowdData(String zoneId, String zoneName, int capacity, int occupancy, double percentage, String status) {}

    private static final List<Zone> ZONES = List.of(
        new Zone("lower-north", "Lower North Stand", "seating", 12000, 0, "Lower"),
        new Zone("lower-south", "Lower South Stand", "seating", 12000, 0, "Lower"),
        new Zone("lower-east", "Lower East Stand", "seating", 10000, 0, "Lower"),
        new Zone("lower-west", "Lower West Stand", "seating", 10000, 0, "Lower"),
        new Zone("upper-north", "Upper North Stand", "seating", 8000, 0, "Upper"),
        new Zone("upper-south", "Upper South Stand", "seating", 8000, 0, "Upper"),
        new Zone("upper-east", "Upper East Stand", "seating", 6000, 0, "Upper"),
        new Zone("upper-west", "Upper West Stand", "seating", 6000, 0, "Upper"),
        new Zone("vip-west", "VIP Lounge West", "vip", 2000, 0, "Premium"),
        new Zone("concourse-n", "North Concourse", "concourse", 5000, 0, "Ground"),
        new Zone("concourse-s", "South Concourse", "concourse", 5000, 0, "Ground"),
        new Zone("food-court-1", "Food Court Alpha", "food", 1500, 0, "Ground"),
        new Zone("food-court-2", "Food Court Beta", "food", 1500, 0, "Upper"),
        new Zone("gate-a", "Gate A Entry", "gate", 3000, 0, "Ground"),
        new Zone("gate-b", "Gate B Entry", "gate", 3000, 0, "Ground"),
        new Zone("gate-c", "Gate C Entry", "gate", 2000, 0, "Ground"),
        new Zone("gate-d", "Gate D Entry", "gate", 2000, 0, "Ground")
    );

    private static final List<Amenity> AMENITIES = List.of(
        new Amenity("rest-1", "Restrooms North Lower", "restroom", "lower-north", "Near section 102, wheelchair accessible"),
        new Amenity("rest-2", "Restrooms South Lower", "restroom", "lower-south", "Near section 202, wheelchair accessible"),
        new Amenity("rest-3", "Restrooms Upper East", "restroom", "upper-east", "Near section 310"),
        new Amenity("med-1", "First Aid Station", "medical", "concourse-n", "Full medical staff on duty"),
        new Amenity("med-2", "First Aid Station South", "medical", "concourse-s", "Emergency care available"),
        new Amenity("food-1", "Stadium Grill", "food", "food-court-1", "Burgers, hot dogs, fries"),
        new Amenity("food-2", "Global Bites", "food", "food-court-1", "International cuisine"),
        new Amenity("food-3", "Pizza Corner", "food", "food-court-2", "Pizza, pasta, salads"),
        new Amenity("food-4", "Taco Stand", "food", "food-court-2", "Tacos, burritos, nachos"),
        new Amenity("drink-1", "Hydration Station", "drink", "concourse-n", "Free water refill station"),
        new Amenity("drink-2", "Hydration Station South", "drink", "concourse-s", "Free water refill station"),
        new Amenity("merch-1", "Official FIFA Store", "merchandise", "concourse-n", "Jerseys, scarves, souvenirs"),
        new Amenity("merch-2", "Team Shop", "merchandise", "concourse-s", "Team-specific merchandise"),
        new Amenity("access-1", "Wheelchair Ramp Gate A", "accessibility", "gate-a", "Ground level access with assistance"),
        new Amenity("access-2", "Elevator East", "accessibility", "lower-east", "Access to all levels"),
        new Amenity("recycle-1", "Recycling Point North", "sustainability", "concourse-n", "Plastics, paper, cans"),
        new Amenity("recycle-2", "Recycling Point South", "sustainability", "concourse-s", "Plastics, paper, cans"),
        new Amenity("charge-1", "Phone Charging Hub", "utility", "concourse-n", "Free USB/wireless charging")
    );

    public List<Map<String, Object>> getMapData() {
        return ZONES.stream().map(z -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", z.id());
            m.put("name", z.name());
            m.put("type", z.type());
            m.put("capacity", z.capacity());
            m.put("level", z.level());
            return m;
        }).toList();
    }

    public List<Map<String, Object>> getAmenities() {
        return AMENITIES.stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.id());
            m.put("name", a.name());
            m.put("type", a.type());
            m.put("zone", a.zone());
            m.put("description", a.description());
            return m;
        }).toList();
    }

    public List<CrowdData> getCrowdDensity() {
        return ZONES.stream().map(z -> {
            double basePct = switch (z.type()) {
                case "seating" -> 0.65;
                case "food" -> 0.55;
                case "concourse" -> 0.45;
                case "gate" -> 0.30;
                case "vip" -> 0.40;
                default -> 0.50;
            };
            double jitter = ThreadLocalRandom.current().nextDouble(-0.15, 0.20);
            double pct = Math.max(0.1, Math.min(0.95, basePct + jitter));
            int occ = (int) (z.capacity() * pct);
            String status = pct > 0.80 ? "high" : pct > 0.50 ? "moderate" : "low";
            return new CrowdData(z.id(), z.name(), z.capacity(), occ, Math.round(pct * 100.0) / 100.0, status);
        }).toList();
    }

    /** Returns a text summary for the AI to use as context */
    public String getContextSummary() {
        StringBuilder sb = new StringBuilder("Stadium: MetLife Stadium, East Rutherford, NJ. Capacity: 82,500.\n\n");
        sb.append("ZONES:\n");
        ZONES.forEach(z -> sb.append("- ").append(z.name()).append(" (").append(z.type()).append(", ").append(z.level()).append(" level, capacity ").append(z.capacity()).append(")\n"));
        sb.append("\nAMENITIES:\n");
        AMENITIES.forEach(a -> sb.append("- ").append(a.name()).append(" [").append(a.type()).append("] in ").append(a.zone()).append(": ").append(a.description()).append("\n"));

        sb.append("\nCURRENT CROWD:\n");
        getCrowdDensity().forEach(c -> sb.append("- ").append(c.zoneName()).append(": ").append((int)(c.percentage()*100)).append("% full (").append(c.status()).append(")\n"));

        return sb.toString();
    }
}
