package com.transport.erp.common.controller;

import com.transport.erp.auth.dto.UserResponse;
import com.transport.erp.auth.service.AuthService;
import com.transport.erp.branch.dto.BranchResponse;
import com.transport.erp.branch.service.BranchService;
import com.transport.erp.common.service.ExcelExportService;
import com.transport.erp.common.service.PdfExportService;
import com.transport.erp.driver.service.DriverService;
import com.transport.erp.trip.service.TripService;
import com.transport.erp.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ExcelExportService excelExportService;
    private final PdfExportService pdfExportService;
    private final VehicleService vehicleService;
    private final DriverService driverService;
    private final AuthService authService;
    private final BranchService branchService;
    private final TripService tripService;

    // ── Excel exports ──────────────────────────────────────────────────────────

    @GetMapping("/{entityType}/excel")
    @PreAuthorize("hasAuthority('DATA_EXPORT')")
    public ResponseEntity<byte[]> exportExcel(@PathVariable String entityType) throws Exception {
        ReportData report = getReportData(entityType);
        byte[] bytes = excelExportService.generate(report.title, report.columns, report.data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + entityType + "_report.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(bytes);
    }

    // ── PDF exports ────────────────────────────────────────────────────────────

    @GetMapping("/{entityType}/pdf")
    @PreAuthorize("hasAuthority('DATA_EXPORT')")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String entityType) throws Exception {
        ReportData report = getReportData(entityType);
        byte[] bytes = pdfExportService.generate(report.title, report.columns, report.data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + entityType + "_report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(bytes);
    }

    // ── Data builders ──────────────────────────────────────────────────────────

    private ReportData getReportData(String entityType) {
        return switch (entityType.toLowerCase()) {
            case "vehicles" -> buildVehicleReport();
            case "drivers" -> buildDriverReport();
            case "users" -> buildUserReport();
            case "branches" -> buildBranchReport();
            case "trips" -> buildTripReport();
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        };
    }

    private ReportData buildVehicleReport() {
        List<String> columns = List.of(
                "registration_number", "vehicle_type", "make", "model",
                "fuel_type", "capacity", "status", "branch_name",
                "insurance_expiry", "fitness_expiry", "permit_expiry");

        var vehicles = vehicleService.getAllVehicles(0, 10000, "registrationNumber");
        List<Map<String, Object>> data = vehicles.getContent().stream()
                .map(v -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("registration_number", v.getRegistrationNumber());
                    row.put("vehicle_type", v.getVehicleType());
                    row.put("make", v.getMake());
                    row.put("model", v.getModel());
                    row.put("fuel_type", v.getFuelType());
                    row.put("capacity", v.getCapacity());
                    row.put("status", v.getStatus());
                    row.put("branch_name", v.getBranchName());
                    row.put("insurance_expiry", v.getInsuranceExpiry());
                    row.put("fitness_expiry", v.getFitnessExpiry());
                    row.put("permit_expiry", v.getPermitExpiry());
                    return row;
                }).collect(Collectors.toList());

        return new ReportData("Vehicles Report", columns, data);
    }

    private ReportData buildDriverReport() {
        List<String> columns = List.of(
                "employee_code", "name", "phone", "status",
                "city", "state", "license_number", "license_type",
                "license_expiry_date", "branch_name");

        var drivers = driverService.getAllDrivers(0, 10000, "name");
        List<Map<String, Object>> data = drivers.getContent().stream()
                .map(d -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("employee_code", d.getEmployeeCode());
                    row.put("name", d.getName());
                    row.put("phone", d.getPhone());
                    row.put("status", d.getStatus());
                    row.put("city", d.getCity());
                    row.put("state", d.getState());
                    row.put("license_number", d.getLicenseNumber());
                    row.put("license_type", d.getLicenseType());
                    row.put("license_expiry_date", d.getLicenseExpiryDate());
                    row.put("branch_name", d.getBranchName());
                    return row;
                }).collect(Collectors.toList());

        return new ReportData("Drivers Report", columns, data);
    }

    private ReportData buildUserReport() {
        List<String> columns = List.of(
                "username", "full_name", "email", "phone",
                "role", "active", "branch_name");

        List<UserResponse> users = authService.getAllUsers();
        List<Map<String, Object>> data = users.stream()
                .map(u -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("username", u.getUsername());
                    row.put("full_name", u.getFullName());
                    row.put("email", u.getEmail());
                    row.put("phone", u.getPhone());
                    row.put("role", u.getRoles() != null ? String.join(", ", u.getRoles()) : "");
                    row.put("active", u.isActive());
                    row.put("branch_name", u.getBranchName());
                    return row;
                }).collect(Collectors.toList());

        return new ReportData("Users Report", columns, data);
    }

    private ReportData buildBranchReport() {
        List<String> columns = List.of(
                "name", "code", "city", "state",
                "phone", "email", "contact_person", "active");

        List<BranchResponse> branches = branchService.getAllBranches();
        List<Map<String, Object>> data = branches.stream()
                .map(b -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("name", b.getName());
                    row.put("code", b.getCode());
                    row.put("city", b.getCity());
                    row.put("state", b.getState());
                    row.put("phone", b.getPhone());
                    row.put("email", b.getEmail());
                    row.put("contact_person", b.getContactPerson());
                    row.put("active", b.isActive());
                    return row;
                }).collect(Collectors.toList());

        return new ReportData("Branches Report", columns, data);
    }

    private ReportData buildTripReport() {
        List<String> columns = List.of(
                "trip_number", "source", "destination", "status",
                "vehicle", "primary_driver", "planned_departure",
                "planned_arrival", "distance_planned");

        var trips = tripService.getAllTrips(0, 10000, "createdAt", null, null);
        List<Map<String, Object>> data = trips.getContent().stream()
                .map(t -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("trip_number", t.getTripNumber());
                    row.put("source", t.getSource());
                    row.put("destination", t.getDestination());
                    row.put("status", t.getStatus());
                    row.put("vehicle", t.getVehicleRegistrationNumber());
                    row.put("primary_driver", t.getPrimaryDriverName());
                    row.put("planned_departure", t.getPlannedDeparture());
                    row.put("planned_arrival", t.getPlannedArrival());
                    row.put("distance_planned", t.getDistancePlanned());
                    return row;
                }).collect(Collectors.toList());

        return new ReportData("Trips Report", columns, data);
    }

    // ── Inner class ────────────────────────────────────────────────────────────

    private record ReportData(String title, List<String> columns, List<Map<String, Object>> data) {
    }
}
