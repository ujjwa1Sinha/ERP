package com.transport.erp.vehicle.domain;

import com.transport.erp.branch.domain.Branch;
import com.transport.erp.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "vehicles", uniqueConstraints = {
        @UniqueConstraint(columnNames = "registration_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle extends BaseEntity {

    @Column(name = "registration_number", nullable = false, length = 20)
    private String registrationNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(length = 50)
    private String make;

    @Column(length = 50)
    private String model;

    @Column(name = "manufacture_year")
    private Integer year;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 20)
    private FuelType fuelType;

    @Column
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.ACTIVE;

    @Column(name = "current_odometer", precision = 12, scale = 2)
    private BigDecimal currentOdometer;

    @Column(name = "chassis_number", length = 25)
    private String chassisNumber;

    @Column(name = "engine_number", length = 25)
    private String engineNumber;

    @Column(name = "gps_device_id", length = 50)
    private String gpsDeviceId;

    @Column(name = "insurance_expiry")
    private LocalDate insuranceExpiry;

    @Column(name = "fitness_expiry")
    private LocalDate fitnessExpiry;

    @Column(name = "permit_expiry")
    private LocalDate permitExpiry;

    @Column(name = "pollution_expiry")
    private LocalDate pollutionExpiry;

    @Column(name = "tax_expiry")
    private LocalDate taxExpiry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;
}
