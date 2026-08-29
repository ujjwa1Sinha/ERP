package com.transport.erp.vehicle.service;

import com.transport.erp.branch.domain.Branch;
import com.transport.erp.branch.repository.BranchRepository;
import com.transport.erp.common.dto.PagedResponse;
import com.transport.erp.common.exception.DuplicateResourceException;
import com.transport.erp.common.exception.ResourceNotFoundException;
import com.transport.erp.vehicle.domain.*;
import com.transport.erp.vehicle.dto.VehicleRequest;
import com.transport.erp.vehicle.dto.VehicleResponse;
import com.transport.erp.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
        if (vehicleRepository.existsByRegistrationNumber(request.getRegistrationNumber())) {
            throw new DuplicateResourceException("Vehicle", "registrationNumber", request.getRegistrationNumber());
        }

        Vehicle vehicle = Vehicle.builder()
                .registrationNumber(request.getRegistrationNumber().toUpperCase().trim())
                .vehicleType(VehicleType.valueOf(request.getVehicleType().toUpperCase()))
                .make(request.getMake())
                .model(request.getModel())
                .year(request.getYear())
                .fuelType(request.getFuelType() != null ? FuelType.valueOf(request.getFuelType().toUpperCase()) : null)
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? VehicleStatus.valueOf(request.getStatus().toUpperCase())
                        : VehicleStatus.ACTIVE)
                .currentOdometer(request.getCurrentOdometer())
                .chassisNumber(request.getChassisNumber())
                .engineNumber(request.getEngineNumber())
                .gpsDeviceId(request.getGpsDeviceId())
                .insuranceExpiry(request.getInsuranceExpiry())
                .fitnessExpiry(request.getFitnessExpiry())
                .permitExpiry(request.getPermitExpiry())
                .pollutionExpiry(request.getPollutionExpiry())
                .taxExpiry(request.getTaxExpiry())
                .build();

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
            vehicle.setBranch(branch);
        }

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Transactional(readOnly = true)
    public PagedResponse<VehicleResponse> getAllVehicles(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Vehicle> vehiclePage = vehicleRepository.findAll(pageable);
        return buildPagedResponse(vehiclePage);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        return mapToResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByRegistration(String registrationNumber) {
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(registrationNumber.toUpperCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "registrationNumber", registrationNumber));
        return mapToResponse(vehicle);
    }

    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByBranch(UUID branchId) {
        return vehicleRepository.findByBranchId(branchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));

        vehicle.setRegistrationNumber(request.getRegistrationNumber().toUpperCase().trim());
        vehicle.setVehicleType(VehicleType.valueOf(request.getVehicleType().toUpperCase()));
        vehicle.setMake(request.getMake());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setFuelType(
                request.getFuelType() != null ? FuelType.valueOf(request.getFuelType().toUpperCase()) : null);
        vehicle.setCapacity(request.getCapacity());
        if (request.getStatus() != null) {
            vehicle.setStatus(VehicleStatus.valueOf(request.getStatus().toUpperCase()));
        }
        vehicle.setCurrentOdometer(request.getCurrentOdometer());
        vehicle.setChassisNumber(request.getChassisNumber());
        vehicle.setEngineNumber(request.getEngineNumber());
        vehicle.setGpsDeviceId(request.getGpsDeviceId());
        vehicle.setInsuranceExpiry(request.getInsuranceExpiry());
        vehicle.setFitnessExpiry(request.getFitnessExpiry());
        vehicle.setPermitExpiry(request.getPermitExpiry());
        vehicle.setPollutionExpiry(request.getPollutionExpiry());
        vehicle.setTaxExpiry(request.getTaxExpiry());

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
            vehicle.setBranch(branch);
        }

        return mapToResponse(vehicleRepository.save(vehicle));
    }

    @Transactional
    public void decommissionVehicle(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle", "id", id));
        vehicle.setStatus(VehicleStatus.DECOMMISSIONED);
        vehicleRepository.save(vehicle);
    }

    private VehicleResponse mapToResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .registrationNumber(vehicle.getRegistrationNumber())
                .vehicleType(vehicle.getVehicleType().name())
                .make(vehicle.getMake())
                .model(vehicle.getModel())
                .year(vehicle.getYear())
                .fuelType(vehicle.getFuelType() != null ? vehicle.getFuelType().name() : null)
                .capacity(vehicle.getCapacity())
                .status(vehicle.getStatus().name())
                .currentOdometer(vehicle.getCurrentOdometer())
                .chassisNumber(vehicle.getChassisNumber())
                .engineNumber(vehicle.getEngineNumber())
                .gpsDeviceId(vehicle.getGpsDeviceId())
                .insuranceExpiry(vehicle.getInsuranceExpiry())
                .fitnessExpiry(vehicle.getFitnessExpiry())
                .permitExpiry(vehicle.getPermitExpiry())
                .pollutionExpiry(vehicle.getPollutionExpiry())
                .taxExpiry(vehicle.getTaxExpiry())
                .branchId(vehicle.getBranch() != null ? vehicle.getBranch().getId() : null)
                .branchName(vehicle.getBranch() != null ? vehicle.getBranch().getName() : null)
                .createdAt(vehicle.getCreatedAt())
                .build();
    }

    private PagedResponse<VehicleResponse> buildPagedResponse(Page<Vehicle> page) {
        List<VehicleResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<VehicleResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
