package com.transport.erp.maintenance.controller;

import com.transport.erp.maintenance.dto.MaintenanceRecordDTO;
import com.transport.erp.maintenance.service.MaintenanceRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceRecordController {

    private final MaintenanceRecordService maintenanceRecordService;

    @GetMapping
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<Page<MaintenanceRecordDTO>> getAllMaintenanceRecords(
            @PageableDefault(sort = "serviceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(maintenanceRecordService.getAllMaintenanceRecords(pageable));
    }

    @GetMapping("/vehicle/{vehicleId}")
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<Page<MaintenanceRecordDTO>> getMaintenanceRecordsByVehicle(
            @PathVariable UUID vehicleId,
            @PageableDefault(sort = "serviceDate", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(maintenanceRecordService.getMaintenanceRecordsByVehicle(vehicleId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_VIEW')")
    public ResponseEntity<MaintenanceRecordDTO> getMaintenanceRecordById(@PathVariable UUID id) {
        return ResponseEntity.ok(maintenanceRecordService.getMaintenanceRecordById(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public ResponseEntity<MaintenanceRecordDTO> createMaintenanceRecord(@Valid @RequestBody MaintenanceRecordDTO dto) {
        return new ResponseEntity<>(maintenanceRecordService.createMaintenanceRecord(dto), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public ResponseEntity<MaintenanceRecordDTO> updateMaintenanceRecord(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceRecordDTO dto) {
        return ResponseEntity.ok(maintenanceRecordService.updateMaintenanceRecord(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('VEHICLE_EDIT')")
    public ResponseEntity<Void> deleteMaintenanceRecord(@PathVariable UUID id) {
        maintenanceRecordService.deleteMaintenanceRecord(id);
        return ResponseEntity.noContent().build();
    }
}
