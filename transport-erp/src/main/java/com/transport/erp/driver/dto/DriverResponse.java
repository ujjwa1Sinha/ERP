package com.transport.erp.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {

    private UUID id;
    private String employeeCode;
    private String name;
    private String phone;
    private String alternatePhone;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
    private String status;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String bloodGroup;
    private String licenseFileUrl;
    private UUID branchId;
    private String branchName;
    private Instant createdAt;

    private List<LicenseInfo> licenses;
    private List<EmergencyContactInfo> emergencyContacts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LicenseInfo {
        private UUID id;
        private String licenseNumber;
        private String licenseType;
        private String issuingAuthority;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private boolean primary;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmergencyContactInfo {
        private UUID id;
        private String name;
        private String relationship;
        private String phone;
        private boolean primary;
    }
}
