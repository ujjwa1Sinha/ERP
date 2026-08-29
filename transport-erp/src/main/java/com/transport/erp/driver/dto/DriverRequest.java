package com.transport.erp.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequest {

    private String employeeCode;

    @NotBlank(message = "Driver name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String alternatePhone;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
    private String status;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String aadharNumber;
    private String panNumber;
    private String bloodGroup;
    private String licenseFileUrl;
    private UUID branchId;

    private List<DriverLicenseRequest> licenses;
    private List<EmergencyContactRequest> emergencyContacts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DriverLicenseRequest {
        private String licenseNumber;
        private String licenseType;
        private String issuingAuthority;
        private LocalDate issueDate;
        private LocalDate expiryDate;
        private boolean primary;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmergencyContactRequest {
        private String name;
        private String relationship;
        private String phone;
        private String alternatePhone;
        private String address;
        private boolean primary;
    }
}
