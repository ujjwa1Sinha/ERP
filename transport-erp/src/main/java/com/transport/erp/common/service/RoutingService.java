package com.transport.erp.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    @Value("${geoapify.api.key}")
    private String apiKey;

    public record OsrmResult(String polyline, BigDecimal distanceKm, Long durationSeconds) {
    }

    private final RestTemplate restTemplate;

    @Cacheable(value = "osrmRoutes", key = "#sourceLat + '_' + #sourceLng + '_' + #destLat + '_' + #destLng")
    public OsrmResult getOsrmPolyline(BigDecimal sourceLat, BigDecimal sourceLng, BigDecimal destLat,
            BigDecimal destLng) {
        try {
            log.info("Requesting Geoapify Route between {},{} and {},{}", sourceLat, sourceLng, destLat, destLng);
            String waypoints = String.format("%s,%s|%s,%s", sourceLat.toPlainString(), sourceLng.toPlainString(), destLat.toPlainString(), destLng.toPlainString());
            String url = org.springframework.web.util.UriComponentsBuilder.fromHttpUrl("https://api.geoapify.com/v1/routing")
                    .queryParam("waypoints", waypoints)
                    .queryParam("mode", "drive")
                    .queryParam("apiKey", apiKey)
                    .build().toUriString();

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode root = response.getBody();

            if (root != null && root.hasNonNull("features")) {
                JsonNode features = root.path("features");
                if (features.isArray() && !features.isEmpty()) {
                    JsonNode route = features.get(0);
                    JsonNode properties = route.path("properties");
                    
                    // Geoapify polyline geometry isn't natively encoded. We can store the JSON coordinates or an empty proxy if not needed for rendering on maps.
                    // For now, if we don't render polyline, we just return empty string, since OSRM encoded was used.
                    String polyline = ""; 
                    
                    double distanceMeters = properties.path("distance").asDouble();
                    Long durationSeconds = properties.path("time").asLong();
                    BigDecimal distanceKm = BigDecimal.valueOf(distanceMeters / 1000.0).setScale(2,
                            java.math.RoundingMode.HALF_UP);
                    return new OsrmResult(polyline, distanceKm, durationSeconds);
                }
            }
            log.warn("Geoapify routing failed to return a valid route.");
            return null;
        } catch (Exception e) {
            log.error("Error fetching Geoapify route: ", e);
            return null;
        }
    }
}
