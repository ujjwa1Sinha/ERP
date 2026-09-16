package com.transport.erp.fuel.domain;

import com.transport.erp.common.domain.BaseEntity;
import com.transport.erp.driver.domain.Driver;
import com.transport.erp.trip.domain.Trip;
import com.transport.erp.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fuel_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FuelTransaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false)
    private Instant date;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal litres;

    @Column(name = "price_per_litre", precision = 10, scale = 2)
    private BigDecimal pricePerLitre;

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "odometer_reading", precision = 12, scale = 2)
    private BigDecimal odometerReading;

    @Column(length = 200)
    private String location;

    @Column(name = "fuel_station", length = 200)
    private String fuelStation;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
