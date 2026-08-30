package com.transport.erp.driver.domain;

import com.transport.erp.branch.domain.Branch;
import com.transport.erp.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver extends BaseEntity {

    @Column(name = "employee_code", unique = true, length = 20)
    private String employeeCode;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "alternate_phone", length = 15)
    private String alternatePhone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "joining_date")
    private LocalDate joiningDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DriverStatus status = DriverStatus.ACTIVE;

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(name = "pin_code", length = 10)
    private String pinCode;

    @Column(name = "aadhar_number", length = 12)
    private String aadharNumber;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Column(name = "license_file_url", length = 500)
    private String licenseFileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    // ── Licence (flattened — one per driver) ─────────────────────
    @Column(name = "license_number", length = 30)
    private String licenseNumber;

    @Column(name = "license_type", length = 20)
    private String licenseType;

    @Column(name = "license_issuing_authority", length = 100)
    private String licenseIssuingAuthority;

    @Column(name = "license_issue_date")
    private LocalDate licenseIssueDate;

    @Column(name = "license_expiry_date")
    private LocalDate licenseExpiryDate;

    // ── Emergency contact (flattened — one per driver) ────────────
    @Column(name = "ec_name", length = 100)
    private String ecName;

    @Column(name = "ec_relationship", length = 50)
    private String ecRelationship;

    @Column(name = "ec_phone", length = 15)
    private String ecPhone;

    @Column(name = "ec_alternate_phone", length = 15)
    private String ecAlternatePhone;

    @Column(name = "ec_address", length = 500)
    private String ecAddress;
}
