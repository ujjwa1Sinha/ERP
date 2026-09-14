package com.transport.erp.common.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Value("${geoapify.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/autocomplete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JsonNode> autocomplete(@RequestParam String q) {
        String url = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl("https://api.geoapify.com/v1/geocode/autocomplete")
                .queryParam("text", q)
                .queryParam("limit", 5)
                .queryParam("apiKey", apiKey)
                .build().toUriString();

        try {
            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
