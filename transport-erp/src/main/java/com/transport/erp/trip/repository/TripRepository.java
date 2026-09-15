package com.transport.erp.trip.repository;

import com.transport.erp.trip.domain.Trip;
import com.transport.erp.trip.domain.TripStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID> {

        @EntityGraph(attributePaths = { "vehicle", "primaryDriver", "secondaryDriver", "sourceBranch",
                        "destinationBranch" })
        Page<Trip> findAll(Pageable pageable);

        @EntityGraph(attributePaths = { "vehicle", "primaryDriver", "secondaryDriver", "sourceBranch",
                        "destinationBranch" })
        Optional<Trip> findByTripNumber(String tripNumber);

        @EntityGraph(attributePaths = { "vehicle", "primaryDriver", "secondaryDriver", "sourceBranch",
                        "destinationBranch" })
        Page<Trip> findByStatus(TripStatus status, Pageable pageable);

        @EntityGraph(attributePaths = { "vehicle", "primaryDriver", "secondaryDriver", "sourceBranch",
                        "destinationBranch" })
        java.util.List<Trip> findByStatusIn(java.util.List<TripStatus> statuses);

        Optional<Trip> findFirstByVehicleIdAndStatusOrderByActualArrivalDesc(UUID vehicleId, TripStatus status);

        @EntityGraph(attributePaths = { "vehicle", "primaryDriver", "secondaryDriver", "sourceBranch",
                        "destinationBranch" })
        Page<Trip> findByVehicleId(UUID vehicleId, Pageable pageable);

        @EntityGraph(attributePaths = { "vehicle", "primaryDriver", "secondaryDriver", "sourceBranch",
                        "destinationBranch" })
        Page<Trip> findByPrimaryDriverId(UUID driverId, Pageable pageable);

        @EntityGraph(attributePaths = { "vehicle", "primaryDriver", "secondaryDriver", "sourceBranch",
                        "destinationBranch" })
        @Query("SELECT t FROM Trip t WHERE t.plannedDeparture >= :from AND t.plannedDeparture <= :to")
        Page<Trip> findByDateRange(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);

        @Query("SELECT t.status, COUNT(t) FROM Trip t GROUP BY t.status")
        java.util.List<Object[]> countByStatusGrouped();

        long countByStatus(TripStatus status);

        @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.primaryDriver.id = :driverId " +
                        "AND t.status NOT IN ('COMPLETED', 'CANCELLED') " +
                        "AND t.plannedDeparture <= :endTime " +
                        "AND t.plannedArrival >= :startTime " +
                        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)")
        boolean hasOverlappingTripForDriver(@Param("driverId") UUID driverId, @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime, @Param("excludeTripId") UUID excludeTripId);

        @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.vehicle.id = :vehicleId " +
                        "AND t.status NOT IN ('COMPLETED', 'CANCELLED') " +
                        "AND t.plannedDeparture <= :endTime " +
                        "AND t.plannedArrival >= :startTime " +
                        "AND (:excludeTripId IS NULL OR t.id != :excludeTripId)")
        boolean hasOverlappingTripForVehicle(@Param("vehicleId") UUID vehicleId, @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime, @Param("excludeTripId") UUID excludeTripId);

        @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.primaryDriver.id = :driverId " +
                        "AND t.status NOT IN ('COMPLETED', 'CANCELLED') " +
                        "AND t.plannedArrival >= :assignedAt")
        boolean hasFutureOrActiveTripForDriver(@Param("driverId") UUID driverId,
                        @Param("assignedAt") Instant assignedAt);

        @Query("SELECT COUNT(t) > 0 FROM Trip t WHERE t.vehicle.id = :vehicleId " +
                        "AND t.status NOT IN ('COMPLETED', 'CANCELLED') " +
                        "AND t.plannedArrival >= :assignedAt")
        boolean hasFutureOrActiveTripForVehicle(@Param("vehicleId") UUID vehicleId,
                        @Param("assignedAt") Instant assignedAt);
}
