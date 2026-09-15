package com.transport.erp.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Service
@Slf4j
public class GeocodingService {

    @Value("${geoapify.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public record GeoResult(BigDecimal lat, BigDecimal lng) {
    }

    public GeoResult validateAndGeocode(String address) {
        try {
            log.info("Requesting Geoapify Geocode for Address: {}", address);
            String url = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl("https://api.geoapify.com/v1/geocode/search")
                    .queryParam("text", address)
                    .queryParam("limit", "1")
                    .queryParam("apiKey", apiKey)
                    .build()
                    .toUriString();

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode root = response.getBody();

            if (root == null || !root.hasNonNull("features")) {
                log.warn("Geocoding failed - Address unmatched on Geoapify.");
                return null;
            }

            JsonNode features = root.get("features");
            if (features.isEmpty()) {
                log.warn("Geocoding failed - Zero spatial features returned.");
                return null;
            }

            JsonNode topResult = features.get(0);
            JsonNode geometry = topResult.get("geometry");

            if (geometry == null) {
                log.warn("Geocoding failed - Missing core geometric configurations.");
                return null;
            }

            JsonNode coords = geometry.get("coordinates");
            BigDecimal lon = new BigDecimal(coords.get(0).asText());
            BigDecimal lat = new BigDecimal(coords.get(1).asText());

            return new GeoResult(lat, lon);
        } catch (Exception e) {
            log.error("Geocoding error", e);
            return null;
        }
    }
}
