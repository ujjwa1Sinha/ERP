package com.transport.erp.maintenance.service;

import com.transport.erp.common.exception.ResourceNotFoundException;
import com.transport.erp.maintenance.domain.MaintenanceRecord;
import com.transport.erp.maintenance.dto.MaintenanceRecordDTO;
import com.transport.erp.maintenance.repository.MaintenanceRecordRepository;
import com.transport.erp.vehicle.domain.Vehicle;
import com.transport.erp.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaintenanceRecordService {

    private final MaintenanceRecordRepository maintenanceRecordRepository;
    private final VehicleRepository vehicleRepository;

    @Transactional(readOnly = true)
    public Page<MaintenanceRecordDTO> getAllMaintenanceRecords(Pageable pageable) {
        return maintenanceRecordRepository.findAll(pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public Page<MaintenanceRecordDTO> getMaintenanceRecordsByVehicle(UUID vehicleId, Pageable pageable) {
        return maintenanceRecordRepository.findByVehicleId(vehicleId, pageable).map(this::mapToDTO);
    }

    @Transactional(readOnly = true)
    public MaintenanceRecordDTO getMaintenanceRecordById(UUID id) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
        return mapToDTO(record);
    }

    @Transactional
    public MaintenanceRecordDTO createMaintenanceRecord(MaintenanceRecordDTO dto) {
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", dto.getVehicleId()));

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicle(vehicle)
                .maintenanceType(dto.getMaintenanceType())
                .serviceDate(dto.getServiceDate())
                .odometerReading(dto.getOdometerReading())
                .vendor(dto.getVendor())
                .cost(dto.getCost())
                .description(dto.getDescription())
                .nextServiceDate(dto.getNextServiceDate())
                .nextServiceOdometer(dto.getNextServiceOdometer())
                .build();

        record = maintenanceRecordRepository.save(record);
        return mapToDTO(record);
    }

    @Transactional
    public MaintenanceRecordDTO updateMaintenanceRecord(UUID id, MaintenanceRecordDTO dto) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));

        if (!record.getVehicle().getId().equals(dto.getVehicleId())) {
            Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", dto.getVehicleId()));
            record.setVehicle(vehicle);
        }

        record.setMaintenanceType(dto.getMaintenanceType());
        record.setServiceDate(dto.getServiceDate());
        record.setOdometerReading(dto.getOdometerReading());
        record.setVendor(dto.getVendor());
        record.setCost(dto.getCost());
        record.setDescription(dto.getDescription());
        record.setNextServiceDate(dto.getNextServiceDate());
        record.setNextServiceOdometer(dto.getNextServiceOdometer());

        record = maintenanceRecordRepository.save(record);
        return mapToDTO(record);
    }

    @Transactional
    public void deleteMaintenanceRecord(UUID id) {
        MaintenanceRecord record = maintenanceRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaintenanceRecord", "id", id));
        maintenanceRecordRepository.delete(record);
    }

    private MaintenanceRecordDTO mapToDTO(MaintenanceRecord entity) {
        return MaintenanceRecordDTO.builder()
                .id(entity.getId())
                .vehicleId(entity.getVehicle().getId())
                .vehicleRegistrationNumber(entity.getVehicle().getRegistrationNumber())
                .maintenanceType(entity.getMaintenanceType())
                .serviceDate(entity.getServiceDate())
                .odometerReading(entity.getOdometerReading())
                .vendor(entity.getVendor())
                .cost(entity.getCost())
                .description(entity.getDescription())
                .nextServiceDate(entity.getNextServiceDate())
                .nextServiceOdometer(entity.getNextServiceOdometer())
                .build();
    }
}
