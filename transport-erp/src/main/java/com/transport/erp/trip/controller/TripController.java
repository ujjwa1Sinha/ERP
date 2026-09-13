package com.transport.erp.trip.controller;

import com.transport.erp.common.dto.ApiResponse;
import com.transport.erp.common.dto.PagedResponse;
import com.transport.erp.trip.dto.TripEventResponse;
import com.transport.erp.trip.dto.TripRequest;
import com.transport.erp.trip.dto.TripResponse;
import com.transport.erp.trip.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // ────────────────────────────── CRUD ──────────────────────────────

    @PostMapping
    @PreAuthorize("hasAuthority('TRIP_CREATE')")
    public ResponseEntity<ApiResponse<TripResponse>> createTrip(@RequestBody TripRequest request) {
        TripResponse response = tripService.createTrip(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip created successfully", response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TRIP_VIEW')")
    public ResponseEntity<ApiResponse<PagedResponse<TripResponse>>> getAllTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(required = false) String status) {
        PagedResponse<TripResponse> response = tripService.getAllTrips(page, size, sortBy, status);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('TRIP_VIEW')")
    public ResponseEntity<ApiResponse<TripResponse>> getTripById(@PathVariable UUID id) {
        TripResponse response = tripService.getTripById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ────────────────────────────── TIMELINE ──────────────────────────────

    @GetMapping("/{id}/timeline")
    @PreAuthorize("hasAuthority('TRIP_VIEW')")
    public ResponseEntity<ApiResponse<List<TripEventResponse>>> getTripTimeline(@PathVariable UUID id) {
        List<TripEventResponse> timeline = tripService.getTripTimeline(id);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    // ────────────────────────────── ASSIGNMENTS ──────────────────────────────

    @PatchMapping("/{id}/assign-vehicle")
    @PreAuthorize("hasAuthority('TRIP_ASSIGN')")
    public ResponseEntity<ApiResponse<TripResponse>> assignVehicle(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        UUID vehicleId = UUID.fromString(body.get("vehicleId"));
        TripResponse response = tripService.assignVehicle(id, vehicleId);
        return ResponseEntity.ok(ApiResponse.success("Vehicle assigned", response));
    }

    @PatchMapping("/{id}/assign-driver")
    @PreAuthorize("hasAuthority('TRIP_ASSIGN')")
    public ResponseEntity<ApiResponse<TripResponse>> assignDriver(
            @PathVariable UUID id, @RequestBody Map<String, String> body) {
        UUID driverId = UUID.fromString(body.get("driverId"));
        TripResponse response = tripService.assignDriver(id, driverId);
        return ResponseEntity.ok(ApiResponse.success("Driver assigned", response));
    }

    // ────────────────────────────── LIFECYCLE ──────────────────────────────

    @PatchMapping("/{id}/start")
    @PreAuthorize("hasAuthority('TRIP_CREATE')")
    public ResponseEntity<ApiResponse<TripResponse>> startTrip(@PathVariable UUID id) {
        TripResponse response = tripService.startTrip(id);
        return ResponseEntity.ok(ApiResponse.success("Trip started", response));
    }

    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('TRIP_CREATE')")
    public ResponseEntity<ApiResponse<TripResponse>> completeTrip(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, Object> body) {
        BigDecimal distanceActual = null;
        if (body != null && body.containsKey("distanceActual")) {
            distanceActual = new BigDecimal(body.get("distanceActual").toString());
        }
        TripResponse response = tripService.completeTrip(id, distanceActual);
        return ResponseEntity.ok(ApiResponse.success("Trip completed", response));
    }

    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('TRIP_CREATE')")
    public ResponseEntity<ApiResponse<TripResponse>> cancelTrip(
            @PathVariable UUID id, @RequestBody(required = false) Map<String, String> body) {
        String reason = (body != null) ? body.get("reason") : null;
        TripResponse response = tripService.cancelTrip(id, reason);
        return ResponseEntity.ok(ApiResponse.success("Trip cancelled", response));
    }

    // ────────────────────────────── STATS ──────────────────────────────

    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('TRIP_VIEW')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getTripStats() {
        Map<String, Long> stats = tripService.getTripStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
