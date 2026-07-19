package com.smartstadium.controller;

import com.smartstadium.service.StadiumDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stadium")
@RequiredArgsConstructor
public class StadiumController {

    private final StadiumDataService stadiumDataService;

    @GetMapping("/map")
    public ResponseEntity<Map<String, Object>> getMap() {
        return ResponseEntity.ok(Map.of(
                "zones", stadiumDataService.getMapData(),
                "amenities", stadiumDataService.getAmenities()
        ));
    }

    @GetMapping("/crowd")
    public ResponseEntity<List<StadiumDataService.CrowdData>> getCrowd() {
        return ResponseEntity.ok(stadiumDataService.getCrowdDensity());
    }
}
