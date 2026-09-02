package com.transport.erp.driver.repository;

import com.transport.erp.driver.domain.Driver;
import com.transport.erp.driver.domain.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DriverRepository extends JpaRepository<Driver, UUID> {

    Optional<Driver> findByEmployeeCode(String employeeCode);

    boolean existsByEmployeeCode(String employeeCode);

    boolean existsByPhone(String phone);

    List<Driver> findByBranchId(UUID branchId);

    Page<Driver> findByBranchId(UUID branchId, Pageable pageable);

    Page<Driver> findByStatus(DriverStatus status, Pageable pageable);

    @Query("SELECT d FROM Driver d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Driver> searchByName(@Param("name") String name, Pageable pageable);

    @Query("SELECT d FROM Driver d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :name, '%')) AND d.branch.id = :branchId")
    Page<Driver> searchByNameAndBranchId(@Param("name") String name, @Param("branchId") UUID branchId,
            Pageable pageable);

    @Query("SELECT COUNT(d) FROM Driver d WHERE d.status = :status")
    long countByStatus(@Param("status") DriverStatus status);
}
