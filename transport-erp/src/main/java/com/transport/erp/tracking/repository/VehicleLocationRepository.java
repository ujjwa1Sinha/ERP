package com.transport.erp.tracking.repository;

import com.transport.erp.tracking.domain.VehicleLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface VehicleLocationRepository extends JpaRepository<VehicleLocation, UUID> {

    Optional<VehicleLocation> findFirstByVehicleIdOrderByRecordedAtDesc(UUID vehicleId);

    List<VehicleLocation> findByTripIdOrderByRecordedAtAsc(UUID tripId);
}
