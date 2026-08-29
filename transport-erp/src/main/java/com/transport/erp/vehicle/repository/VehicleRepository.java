package com.transport.erp.vehicle.repository;

import com.transport.erp.vehicle.domain.Vehicle;
import com.transport.erp.vehicle.domain.VehicleStatus;
import com.transport.erp.vehicle.domain.VehicleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, UUID> {

    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);

    boolean existsByRegistrationNumber(String registrationNumber);

    List<Vehicle> findByBranchId(UUID branchId);

    Page<Vehicle> findByStatus(VehicleStatus status, Pageable pageable);

    Page<Vehicle> findByVehicleType(VehicleType vehicleType, Pageable pageable);

    @Query("SELECT v FROM Vehicle v WHERE v.insuranceExpiry <= :date OR v.fitnessExpiry <= :date " +
            "OR v.permitExpiry <= :date OR v.pollutionExpiry <= :date")
    List<Vehicle> findVehiclesWithExpiringDocuments(@Param("date") LocalDate date);

    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status")
    long countByStatus(@Param("status") VehicleStatus status);
}
