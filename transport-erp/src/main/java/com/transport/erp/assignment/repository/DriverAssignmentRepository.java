package com.transport.erp.assignment.repository;

import com.transport.erp.assignment.domain.DriverAssignment;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverAssignmentRepository extends JpaRepository<DriverAssignment, UUID> {

        List<DriverAssignment> findByDriverId(UUID driverId);

        List<DriverAssignment> findByVehicleId(UUID vehicleId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT da FROM DriverAssignment da WHERE da.vehicle.id = :vehicleId AND da.releasedAt IS NULL")
        Optional<DriverAssignment> findActiveAssignmentByVehicle(@Param("vehicleId") UUID vehicleId);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT da FROM DriverAssignment da WHERE da.driver.id = :driverId AND da.releasedAt IS NULL")
        Optional<DriverAssignment> findActiveAssignmentByDriver(@Param("driverId") UUID driverId);

        @Query("SELECT da FROM DriverAssignment da WHERE da.vehicle.id = :vehicleId " +
                        "AND da.assignedAt <= :timestamp AND (da.releasedAt IS NULL OR da.releasedAt >= :timestamp)")
        List<DriverAssignment> findAssignmentsAtTime(
                        @Param("vehicleId") UUID vehicleId, @Param("timestamp") Instant timestamp);

        @Query("SELECT da FROM DriverAssignment da WHERE da.releasedAt IS NULL")
        List<DriverAssignment> findAllActiveAssignments();

        @Query("SELECT da FROM DriverAssignment da WHERE da.releasedAt IS NULL AND da.vehicle.branch.id = :branchId")
        List<DriverAssignment> findAllActiveAssignmentsByBranch(@Param("branchId") UUID branchId);

        @Query("SELECT da FROM DriverAssignment da WHERE da.releasedAt IS NOT NULL ORDER BY da.releasedAt DESC")
        List<DriverAssignment> findAllReleasedAssignments();

        @Query("SELECT da FROM DriverAssignment da WHERE da.releasedAt IS NOT NULL AND da.vehicle.branch.id = :branchId ORDER BY da.releasedAt DESC")
        List<DriverAssignment> findAllReleasedAssignmentsByBranch(@Param("branchId") UUID branchId);

        List<DriverAssignment> findByTripId(UUID tripId);
}
