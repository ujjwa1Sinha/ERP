package com.transport.erp.driver.service;

import com.transport.erp.branch.domain.Branch;
import com.transport.erp.branch.repository.BranchRepository;
import com.transport.erp.common.dto.PagedResponse;
import com.transport.erp.common.exception.DuplicateResourceException;
import com.transport.erp.common.exception.ResourceNotFoundException;
import com.transport.erp.driver.domain.*;
import com.transport.erp.driver.dto.DriverRequest;
import com.transport.erp.driver.dto.DriverResponse;
import com.transport.erp.driver.repository.DriverRepository;
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
public class DriverService {

    private final DriverRepository driverRepository;
    private final BranchRepository branchRepository;

    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        if (request.getEmployeeCode() != null && driverRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new DuplicateResourceException("Driver", "employeeCode", request.getEmployeeCode());
        }
        if (driverRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Driver", "phone", request.getPhone());
        }

        Driver driver = Driver.builder()
                .employeeCode(request.getEmployeeCode())
                .name(request.getName())
                .phone(request.getPhone())
                .alternatePhone(request.getAlternatePhone())
                .dateOfBirth(request.getDateOfBirth())
                .joiningDate(request.getJoiningDate())
                .status(request.getStatus() != null
                        ? DriverStatus.valueOf(request.getStatus().toUpperCase())
                        : DriverStatus.ACTIVE)
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .aadharNumber(request.getAadharNumber())
                .panNumber(request.getPanNumber())
                .bloodGroup(request.getBloodGroup())
                .licenseFileUrl(request.getLicenseFileUrl())
                .build();

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
            driver.setBranch(branch);
        }

        // Add licenses
        if (request.getLicenses() != null) {
            for (DriverRequest.DriverLicenseRequest licReq : request.getLicenses()) {
                DriverLicense license = DriverLicense.builder()
                        .driver(driver)
                        .licenseNumber(licReq.getLicenseNumber())
                        .licenseType(licReq.getLicenseType())
                        .issuingAuthority(licReq.getIssuingAuthority())
                        .issueDate(licReq.getIssueDate())
                        .expiryDate(licReq.getExpiryDate())
                        .primary(licReq.isPrimary())
                        .build();
                driver.getLicenses().add(license);
            }
        }

        // Add emergency contacts
        if (request.getEmergencyContacts() != null) {
            for (DriverRequest.EmergencyContactRequest ecReq : request.getEmergencyContacts()) {
                DriverEmergencyContact contact = DriverEmergencyContact.builder()
                        .driver(driver)
                        .name(ecReq.getName())
                        .relationship(ecReq.getRelationship())
                        .phone(ecReq.getPhone())
                        .alternatePhone(ecReq.getAlternatePhone())
                        .address(ecReq.getAddress())
                        .primary(ecReq.isPrimary())
                        .build();
                driver.getEmergencyContacts().add(contact);
            }
        }

        return mapToResponse(driverRepository.save(driver));
    }

    @Transactional(readOnly = true)
    public PagedResponse<DriverResponse> getAllDrivers(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).ascending());
        Page<Driver> driverPage = driverRepository.findAll(pageable);
        return buildPagedResponse(driverPage);
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriverById(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        return mapToResponse(driver);
    }

    @Transactional(readOnly = true)
    public DriverResponse getDriverByEmployeeCode(String employeeCode) {
        Driver driver = driverRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "employeeCode", employeeCode));
        return mapToResponse(driver);
    }

    @Transactional(readOnly = true)
    public List<DriverResponse> getDriversByBranch(UUID branchId) {
        return driverRepository.findByBranchId(branchId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PagedResponse<DriverResponse> searchDrivers(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Driver> driverPage = driverRepository.searchByName(name, pageable);
        return buildPagedResponse(driverPage);
    }

    @Transactional
    public DriverResponse updateDriver(UUID id, DriverRequest request) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));

        driver.setEmployeeCode(request.getEmployeeCode());
        driver.setName(request.getName());
        driver.setPhone(request.getPhone());
        driver.setAlternatePhone(request.getAlternatePhone());
        driver.setDateOfBirth(request.getDateOfBirth());
        driver.setJoiningDate(request.getJoiningDate());
        if (request.getStatus() != null) {
            driver.setStatus(DriverStatus.valueOf(request.getStatus().toUpperCase()));
        }
        driver.setAddress(request.getAddress());
        driver.setCity(request.getCity());
        driver.setState(request.getState());
        driver.setPinCode(request.getPinCode());
        driver.setAadharNumber(request.getAadharNumber());
        driver.setPanNumber(request.getPanNumber());
        driver.setBloodGroup(request.getBloodGroup());
        if (request.getLicenseFileUrl() != null) {
            driver.setLicenseFileUrl(request.getLicenseFileUrl());
        }

        if (request.getBranchId() != null) {
            Branch branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", request.getBranchId()));
            driver.setBranch(branch);
        }

        // Update licenses
        if (request.getLicenses() != null) {
            driver.getLicenses().clear();
            for (DriverRequest.DriverLicenseRequest licReq : request.getLicenses()) {
                DriverLicense license = DriverLicense.builder()
                        .driver(driver)
                        .licenseNumber(licReq.getLicenseNumber())
                        .licenseType(licReq.getLicenseType())
                        .issuingAuthority(licReq.getIssuingAuthority())
                        .issueDate(licReq.getIssueDate())
                        .expiryDate(licReq.getExpiryDate())
                        .primary(licReq.isPrimary())
                        .build();
                driver.getLicenses().add(license);
            }
        }

        // Update emergency contacts
        if (request.getEmergencyContacts() != null) {
            driver.getEmergencyContacts().clear();
            for (DriverRequest.EmergencyContactRequest ecReq : request.getEmergencyContacts()) {
                DriverEmergencyContact contact = DriverEmergencyContact.builder()
                        .driver(driver)
                        .name(ecReq.getName())
                        .relationship(ecReq.getRelationship())
                        .phone(ecReq.getPhone())
                        .alternatePhone(ecReq.getAlternatePhone())
                        .address(ecReq.getAddress())
                        .primary(ecReq.isPrimary())
                        .build();
                driver.getEmergencyContacts().add(contact);
            }
        }

        return mapToResponse(driverRepository.save(driver));
    }

    @Transactional
    public void updateDriverStatus(UUID id, String status) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        driver.setStatus(DriverStatus.valueOf(status.toUpperCase()));
        driverRepository.save(driver);
    }

    @Transactional
    public void deleteDriver(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", "id", id));
        driverRepository.delete(driver);
    }

    private DriverResponse mapToResponse(Driver driver) {
        List<DriverResponse.LicenseInfo> licenseInfos = driver.getLicenses().stream()
                .map(lic -> DriverResponse.LicenseInfo.builder()
                        .id(lic.getId())
                        .licenseNumber(lic.getLicenseNumber())
                        .licenseType(lic.getLicenseType())
                        .issuingAuthority(lic.getIssuingAuthority())
                        .issueDate(lic.getIssueDate())
                        .expiryDate(lic.getExpiryDate())
                        .primary(lic.isPrimary())
                        .build())
                .collect(Collectors.toList());

        List<DriverResponse.EmergencyContactInfo> contactInfos = driver.getEmergencyContacts().stream()
                .map(ec -> DriverResponse.EmergencyContactInfo.builder()
                        .id(ec.getId())
                        .name(ec.getName())
                        .relationship(ec.getRelationship())
                        .phone(ec.getPhone())
                        .primary(ec.isPrimary())
                        .build())
                .collect(Collectors.toList());

        return DriverResponse.builder()
                .id(driver.getId())
                .employeeCode(driver.getEmployeeCode())
                .name(driver.getName())
                .phone(driver.getPhone())
                .alternatePhone(driver.getAlternatePhone())
                .dateOfBirth(driver.getDateOfBirth())
                .joiningDate(driver.getJoiningDate())
                .status(driver.getStatus().name())
                .address(driver.getAddress())
                .city(driver.getCity())
                .state(driver.getState())
                .pinCode(driver.getPinCode())
                .bloodGroup(driver.getBloodGroup())
                .licenseFileUrl(driver.getLicenseFileUrl())
                .branchId(driver.getBranch() != null ? driver.getBranch().getId() : null)
                .branchName(driver.getBranch() != null ? driver.getBranch().getName() : null)
                .createdAt(driver.getCreatedAt())
                .licenses(licenseInfos)
                .emergencyContacts(contactInfos)
                .build();
    }

    private PagedResponse<DriverResponse> buildPagedResponse(Page<Driver> page) {
        List<DriverResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<DriverResponse>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
