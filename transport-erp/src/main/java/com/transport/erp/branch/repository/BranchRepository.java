package com.transport.erp.branch.repository;

import com.transport.erp.branch.domain.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BranchRepository extends JpaRepository<Branch, UUID> {

    Optional<Branch> findByCode(String code);

    List<Branch> findByActiveTrue();

    boolean existsByCode(String code);
}
