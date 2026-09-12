package com.transport.erp.tracking.domain;

import com.transport.erp.common.domain.BaseEntity;
import com.transport.erp.trip.domain.Trip;
import com.transport.erp.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

@Entity
@Table(name = "vehicle_locations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleLocation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id")
    private Trip trip;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(precision = 5, scale = 2)
    private BigDecimal speed;

    @Column(precision = 5, scale = 2)
    private BigDecimal heading;

    @Column(precision = 8, scale = 2)
    private BigDecimal accuracy;

    @Column(name = "recorded_at", nullable = false)
    private ZonedDateTime recordedAt;

    public BigDecimal getSpeed() {
        return speed;
    }

    public BigDecimal getHeading() {
        return heading;
    }

    public BigDecimal getAccuracy() {
        return accuracy;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }
}
