package com.transport.erp.trip.domain;

import com.transport.erp.branch.domain.Branch;
import com.transport.erp.common.domain.BaseEntity;
import com.transport.erp.driver.domain.Driver;
import com.transport.erp.vehicle.domain.Vehicle;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip extends BaseEntity {

    @Column(name = "trip_number", nullable = false, unique = true, length = 30)
    private String tripNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_driver_id")
    private Driver primaryDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "secondary_driver_id")
    private Driver secondaryDriver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_branch_id")
    private Branch sourceBranch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_branch_id")
    private Branch destinationBranch;

    @Column(nullable = false, length = 200)
    private String source;

    @Column(nullable = false, length = 200)
    private String destination;

    @Column(name = "planned_departure")
    private Instant plannedDeparture;

    @Column(name = "planned_arrival")
    private Instant plannedArrival;

    @Column(name = "actual_departure")
    private Instant actualDeparture;

    @Column(name = "actual_arrival")
    private Instant actualArrival;

    @Column(name = "idempotency_key", unique = true, length = 100)
    private String idempotencyKey;

    @Column(name = "source_lat", precision = 9, scale = 6)
    private BigDecimal sourceLat;

    @Column(name = "source_lng", precision = 9, scale = 6)
    private BigDecimal sourceLng;

    @Column(name = "dest_lat", precision = 9, scale = 6)
    private BigDecimal destLat;

    @Column(name = "dest_lng", precision = 9, scale = 6)
    private BigDecimal destLng;

    @Column(name = "wandered_alert_sent")
    @Builder.Default
    private Boolean wanderedAlertSent = false;

    @Column(name = "route_polyline", columnDefinition = "TEXT")
    private String routePolyline;

    @Column(name = "deviation_alert_sent")
    private Boolean deviationAlertSent = false;

    @Column(name = "last_halt_start_time")
    private LocalDateTime lastHaltStartTime;

    @Column(name = "frequent_halts_count")
    private Integer frequentHaltsCount = 0;

    @Column(name = "last_halt_alert_time")
    private LocalDateTime lastHaltAlertTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private TripStatus status = TripStatus.PLANNED;

    @Column(name = "trip_type", length = 30)
    private String tripType;

    @Column(name = "distance_planned", precision = 10, scale = 2)
    private BigDecimal distancePlanned;

    @Column(name = "distance_actual", precision = 10, scale = 2)
    private BigDecimal distanceActual;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("eventTimestamp ASC")
    @Builder.Default
    private List<TripEvent> events = new ArrayList<>();
}
