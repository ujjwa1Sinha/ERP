package com.transport.erp.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoutingService {

    public record OsrmResult(String polyline, BigDecimal distanceKm) {
    }

    private final RestTemplate restTemplate;

    public OsrmResult getOsrmPolyline(BigDecimal sourceLat, BigDecimal sourceLng, BigDecimal destLat,
            BigDecimal destLng) {
        try {
            log.info("Requesting OSRM Route between {},{} and {},{}", sourceLat, sourceLng, destLat, destLng);
            String url = String.format("http://router.project-osrm.org/route/v1/driving/%s,%s;%s,%s?overview=full",
                    sourceLng.toPlainString(), sourceLat.toPlainString(),
                    destLng.toPlainString(), destLat.toPlainString());

            ResponseEntity<JsonNode> response = restTemplate.getForEntity(url, JsonNode.class);
            JsonNode root = response.getBody();

            if (root != null && "Ok".equals(root.path("code").asText())) {
                JsonNode routes = root.path("routes");
                if (routes.isArray() && !routes.isEmpty()) {
                    JsonNode route = routes.get(0);
                    String polyline = route.path("geometry").asText();
                    double distanceMeters = route.path("distance").asDouble();
                    BigDecimal distanceKm = BigDecimal.valueOf(distanceMeters / 1000.0).setScale(2,
                            java.math.RoundingMode.HALF_UP);
                    return new OsrmResult(polyline, distanceKm);
                }
            }
            log.warn("OSRM routing failed to return a valid geometry.");
            return null;
        } catch (Exception e) {
            log.error("Error fetching OSRM route: ", e);
            return null;
        }
    }
}
