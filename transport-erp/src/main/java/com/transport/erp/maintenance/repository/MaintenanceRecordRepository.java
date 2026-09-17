package com.transport.erp.maintenance.repository;

import com.transport.erp.maintenance.domain.MaintenanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MaintenanceRecordRepository extends JpaRepository<MaintenanceRecord, UUID> {
    Page<MaintenanceRecord> findByVehicleId(UUID vehicleId, Pageable pageable);
}
