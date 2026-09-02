package com.transport.erp.branch.service;

import com.transport.erp.branch.domain.Branch;
import com.transport.erp.branch.dto.BranchRequest;
import com.transport.erp.branch.dto.BranchResponse;
import com.transport.erp.branch.repository.BranchRepository;
import com.transport.erp.common.exception.DuplicateResourceException;
import com.transport.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.transport.erp.auth.security.SecurityService;

@Service
@RequiredArgsConstructor
public class BranchService {

    private final BranchRepository branchRepository;
    private final SecurityService securityService;

    @Transactional
    public BranchResponse createBranch(BranchRequest request) {
        if (request.getCode() != null && branchRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Branch", "code", request.getCode());
        }

        Branch branch = Branch.builder()
                .name(request.getName())
                .code(request.getCode())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .pinCode(request.getPinCode())
                .phone(request.getPhone())
                .email(request.getEmail())
                .contactPerson(request.getContactPerson())
                .active(true)
                .build();

        return mapToResponse(branchRepository.save(branch));
    }

    @Transactional(readOnly = true)
    public List<BranchResponse> getAllBranches() {
        com.transport.erp.auth.domain.User currentUser = securityService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == com.transport.erp.auth.domain.RoleType.BRANCH_ADMIN
                && currentUser.getBranch() != null) {
            return java.util.List.of(mapToResponse(currentUser.getBranch()));
        }

        return branchRepository.findByActiveTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BranchResponse getBranchById(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        return mapToResponse(branch);
    }

    @Transactional
    public BranchResponse updateBranch(UUID id, BranchRequest request) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));

        branch.setName(request.getName());
        branch.setCode(request.getCode());
        branch.setAddress(request.getAddress());
        branch.setCity(request.getCity());
        branch.setState(request.getState());
        branch.setPinCode(request.getPinCode());
        branch.setPhone(request.getPhone());
        branch.setEmail(request.getEmail());
        branch.setContactPerson(request.getContactPerson());

        return mapToResponse(branchRepository.save(branch));
    }

    @Transactional
    public void deactivateBranch(UUID id) {
        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Branch", "id", id));
        branch.setActive(false);
        branchRepository.save(branch);
    }

    private BranchResponse mapToResponse(Branch branch) {
        return BranchResponse.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .address(branch.getAddress())
                .city(branch.getCity())
                .state(branch.getState())
                .pinCode(branch.getPinCode())
                .phone(branch.getPhone())
                .email(branch.getEmail())
                .contactPerson(branch.getContactPerson())
                .active(branch.isActive())
                .createdAt(branch.getCreatedAt())
                .build();
    }
}
