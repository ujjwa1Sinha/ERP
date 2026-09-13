package com.transport.erp.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;

@Service
@Slf4j
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();

    public record GeoResult(BigDecimal lat, BigDecimal lng) {
    }

    public GeoResult validateAndGeocode(String address) {
        try {
            log.info("Requesting Photon Geocode validation for Address: {}", address);
            String url = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl("https://photon.komoot.io/api/")
                    .queryParam("q", address)
                    .queryParam("limit", "1")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "TransportERP-Phase7/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.GET, entity, JsonNode.class);
            JsonNode root = response.getBody();

            if (root == null || !root.hasNonNull("features")) {
                log.warn("Geocoding failed - Address unmatched on Photon.");
                return null;
            }

            JsonNode features = root.get("features");
            if (features.isEmpty()) {
                log.warn("Geocoding failed - Zero spatial features returned.");
                return null;
            }

            JsonNode topResult = features.get(0);
            JsonNode geometry = topResult.get("geometry");
            JsonNode properties = topResult.get("properties");

            if (geometry == null || properties == null) {
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
