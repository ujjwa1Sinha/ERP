package com.transport.erp.common.util;

import com.transport.erp.common.service.GeocodingService.GeoResult;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PolylineUtils {

    /**
     * Decodes a Google-style Polyline string into a List of Coordinate points.
     */
    public static List<GeoResult> decodePolyline(String encoded) {
        List<GeoResult> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            poly.add(new GeoResult(
                    BigDecimal.valueOf(((double) lat / 1E5)),
                    BigDecimal.valueOf(((double) lng / 1E5))));
        }
        return poly;
    }

    /**
     * Finds the minimum distance from point P to the nearest segment on the
     * polyline.
     * To handle sparse points, it interpolates the segment into small chunks.
     */
    public static double minimumDistanceToRoute(double pingLat, double pingLng, List<GeoResult> routePoints) {
        if (routePoints == null || routePoints.isEmpty())
            return Double.MAX_VALUE;
        if (routePoints.size() == 1) {
            return GeoUtils.haversine(
                    pingLat, pingLng,
                    routePoints.get(0).lat().doubleValue(), routePoints.get(0).lng().doubleValue());
        }

        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < routePoints.size() - 1; i++) {
            GeoResult p1 = routePoints.get(i);
            GeoResult p2 = routePoints.get(i + 1);

            double lat1 = p1.lat().doubleValue();
            double lng1 = p1.lng().doubleValue();
            double lat2 = p2.lat().doubleValue();
            double lng2 = p2.lng().doubleValue();

            double segmentLength = GeoUtils.haversine(lat1, lng1, lat2, lng2);
            int segments = Math.max(1, (int) Math.ceil(segmentLength / 0.1));

            for (int k = 0; k <= segments; k++) {
                double fraction = (double) k / segments;
                double interpLat = lat1 + (lat2 - lat1) * fraction;
                double interpLng = lng1 + (lng2 - lng1) * fraction;

                double dist = GeoUtils.haversine(pingLat, pingLng, interpLat, interpLng);
                if (dist < minDistance) {
                    minDistance = dist;
                }
            }
        }
        return minDistance;
    }
}
