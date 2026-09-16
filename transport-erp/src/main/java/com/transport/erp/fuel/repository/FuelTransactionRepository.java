package com.transport.erp.fuel.repository;

import com.transport.erp.fuel.domain.FuelTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FuelTransactionRepository extends JpaRepository<FuelTransaction, UUID> {

    @EntityGraph(attributePaths = { "vehicle", "driver", "trip" })
    Page<FuelTransaction> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "vehicle", "driver", "trip" })
    Page<FuelTransaction> findByVehicleId(UUID vehicleId, Pageable pageable);

    @EntityGraph(attributePaths = { "vehicle", "driver" })
    Page<FuelTransaction> findByDriverId(UUID driverId, Pageable pageable);
}
