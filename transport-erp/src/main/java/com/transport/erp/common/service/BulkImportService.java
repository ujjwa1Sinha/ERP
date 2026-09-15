package com.transport.erp.common.service;

import com.transport.erp.auth.dto.RegisterRequest;
import com.transport.erp.auth.service.AuthService;
import com.transport.erp.branch.domain.Branch;
import com.transport.erp.branch.dto.BranchRequest;
import com.transport.erp.branch.repository.BranchRepository;
import com.transport.erp.branch.service.BranchService;
import com.transport.erp.common.dto.BulkImportResult;
import com.transport.erp.common.dto.BulkImportResult.RowError;
import com.transport.erp.driver.dto.DriverRequest;
import com.transport.erp.driver.service.DriverService;
import com.transport.erp.vehicle.dto.VehicleRequest;
import com.transport.erp.vehicle.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BulkImportService {

    private final DriverService driverService;
    private final VehicleService vehicleService;
    private final AuthService authService;
    private final BranchService branchService;
    private final BranchRepository branchRepository;

    public BulkImportResult importData(String entityType, List<Map<String, String>> rows) {
        return switch (entityType.toLowerCase()) {
            case "drivers" -> importDrivers(rows);
            case "vehicles" -> importVehicles(rows);
            case "users" -> importUsers(rows);
            case "branches" -> importBranches(rows);
            default -> throw new IllegalArgumentException("Unsupported entity type: " + entityType);
        };
    }

    // ── Drivers ────────────────────────────────────────────────────────────────

    private BulkImportResult importDrivers(List<Map<String, String>> rows) {
        BulkImportResult result = BulkImportResult.builder()
                .totalRows(rows.size()).successCount(0).errorCount(0).errors(new ArrayList<>()).build();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2; // Excel row (1-indexed header + data)
            Map<String, String> row = rows.get(i);
            try {
                DriverRequest req = new DriverRequest();
                req.setEmployeeCode(val(row, "employee_code"));
                req.setName(val(row, "name"));
                req.setPhone(val(row, "phone"));
                req.setAlternatePhone(val(row, "alternate_phone"));
                req.setDateOfBirth(parseDate(val(row, "date_of_birth")));
                req.setJoiningDate(parseDate(val(row, "joining_date")));
                req.setStatus(val(row, "status"));
                req.setAddress(val(row, "address"));
                req.setCity(val(row, "city"));
                req.setState(val(row, "state"));
                req.setPinCode(val(row, "pin_code"));
                req.setAadharNumber(val(row, "aadhar_number"));
                req.setPanNumber(val(row, "pan_number"));
                req.setBloodGroup(val(row, "blood_group"));
                req.setLicenseNumber(val(row, "license_number"));
                req.setLicenseType(val(row, "license_type"));
                req.setLicenseIssuingAuthority(val(row, "license_issuing_authority"));
                req.setLicenseIssueDate(parseDate(val(row, "license_issue_date")));
                req.setLicenseExpiryDate(parseDate(val(row, "license_expiry_date")));
                req.setEcName(val(row, "ec_name"));
                req.setEcRelationship(val(row, "ec_relationship"));
                req.setEcPhone(val(row, "ec_phone"));
                req.setEcAlternatePhone(val(row, "ec_alternate_phone"));
                req.setEcAddress(val(row, "ec_address"));
                req.setBranchId(resolveBranchId(val(row, "branch_name")));

                driverService.createDriver(req);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setErrorCount(result.getErrorCount() + 1);
                result.getErrors().add(RowError.builder()
                        .row(rowNum).message(e.getMessage()).build());
                log.warn("Import error at row {}: {}", rowNum, e.getMessage());
            }
        }
        return result;
    }

    // ── Vehicles ───────────────────────────────────────────────────────────────

    private BulkImportResult importVehicles(List<Map<String, String>> rows) {
        BulkImportResult result = BulkImportResult.builder()
                .totalRows(rows.size()).successCount(0).errorCount(0).errors(new ArrayList<>()).build();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            Map<String, String> row = rows.get(i);
            try {
                VehicleRequest req = new VehicleRequest();
                req.setRegistrationNumber(val(row, "registration_number"));
                req.setVehicleType(val(row, "vehicle_type"));
                req.setMake(val(row, "make"));
                req.setModel(val(row, "model"));
                req.setYear(parseInteger(val(row, "year")));
                req.setFuelType(val(row, "fuel_type"));
                req.setCapacity(parseInteger(val(row, "capacity")));
                req.setChassisNumber(val(row, "chassis_number"));
                req.setEngineNumber(val(row, "engine_number"));
                req.setGpsDeviceId(val(row, "gps_device_id"));
                req.setInsuranceExpiry(parseDate(val(row, "insurance_expiry")));
                req.setFitnessExpiry(parseDate(val(row, "fitness_expiry")));
                req.setPermitExpiry(parseDate(val(row, "permit_expiry")));
                req.setPollutionExpiry(parseDate(val(row, "pollution_expiry")));
                req.setTaxExpiry(parseDate(val(row, "tax_expiry")));
                req.setBranchId(resolveBranchId(val(row, "branch_name")));

                vehicleService.createVehicle(req);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setErrorCount(result.getErrorCount() + 1);
                result.getErrors().add(RowError.builder()
                        .row(rowNum).message(e.getMessage()).build());
                log.warn("Import error at row {}: {}", rowNum, e.getMessage());
            }
        }
        return result;
    }

    // ── Users ──────────────────────────────────────────────────────────────────

    private BulkImportResult importUsers(List<Map<String, String>> rows) {
        BulkImportResult result = BulkImportResult.builder()
                .totalRows(rows.size()).successCount(0).errorCount(0).errors(new ArrayList<>()).build();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            Map<String, String> row = rows.get(i);
            try {
                RegisterRequest req = new RegisterRequest();
                req.setUsername(val(row, "username"));
                req.setPassword(val(row, "password"));
                req.setEmail(val(row, "email"));
                req.setFullName(val(row, "full_name"));
                req.setPhone(val(row, "phone"));
                String role = val(row, "role");
                if (role != null && !role.isEmpty()) {
                    req.setRoles(Set.of(role.toUpperCase()));
                }
                req.setBranchId(resolveBranchId(val(row, "branch_name")));

                authService.register(req);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setErrorCount(result.getErrorCount() + 1);
                result.getErrors().add(RowError.builder()
                        .row(rowNum).message(e.getMessage()).build());
                log.warn("Import error at row {}: {}", rowNum, e.getMessage());
            }
        }
        return result;
    }

    // ── Branches ───────────────────────────────────────────────────────────────

    private BulkImportResult importBranches(List<Map<String, String>> rows) {
        BulkImportResult result = BulkImportResult.builder()
                .totalRows(rows.size()).successCount(0).errorCount(0).errors(new ArrayList<>()).build();

        for (int i = 0; i < rows.size(); i++) {
            int rowNum = i + 2;
            Map<String, String> row = rows.get(i);
            try {
                BranchRequest req = new BranchRequest();
                req.setName(val(row, "name"));
                req.setCode(val(row, "code"));
                req.setAddress(val(row, "address"));
                req.setCity(val(row, "city"));
                req.setState(val(row, "state"));
                req.setPinCode(val(row, "pin_code"));
                req.setPhone(val(row, "phone"));
                req.setEmail(val(row, "email"));
                req.setContactPerson(val(row, "contact_person"));

                branchService.createBranch(req);
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                result.setErrorCount(result.getErrorCount() + 1);
                result.getErrors().add(RowError.builder()
                        .row(rowNum).message(e.getMessage()).build());
                log.warn("Import error at row {}: {}", rowNum, e.getMessage());
            }
        }
        return result;
    }

    // ── Helpers ─────────────────────────────────────────────────────────────────

    private String val(Map<String, String> row, String key) {
        String v = row.get(key);
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format: '" + value + "'. Expected yyyy-MM-dd");
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank())
            return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: '" + value + "'");
        }
    }

    private UUID resolveBranchId(String branchName) {
        if (branchName == null || branchName.isBlank())
            return null;
        Optional<Branch> branch = branchRepository.findByNameIgnoreCase(branchName.trim());
        if (branch.isEmpty()) {
            throw new IllegalArgumentException("Branch not found: '" + branchName + "'");
        }
        return branch.get().getId();
    }
}
