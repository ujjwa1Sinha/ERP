package com.transport.erp.maintenance.domain;

import com.transport.erp.common.domain.BaseEntity;
import com.transport.erp.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "maintenance_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaintenanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "maintenance_type", nullable = false, length = 50)
    private String maintenanceType;

    @Column(name = "service_date", nullable = false)
    private Instant serviceDate;

    @Column(name = "odometer_reading", nullable = false, precision = 12, scale = 2)
    private BigDecimal odometerReading;

    @Column(length = 200)
    private String vendor;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal cost;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "next_service_date")
    private Instant nextServiceDate;

    @Column(name = "next_service_odometer", precision = 12, scale = 2)
    private BigDecimal nextServiceOdometer;
}
