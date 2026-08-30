package com.transport.erp.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    // ── License fields ───────────────────────
    private String licenseNumber;
    private String licenseType;
    private String licenseIssuingAuthority;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;

    // ── Emergency contact fields ─────────────
    private String ecName;
    private String ecRelationship;
    private String ecPhone;
    private String ecAlternatePhone;
    private String ecAddress;
}
