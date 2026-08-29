package com.transport.erp.auth.service;

import com.transport.erp.auth.domain.Role;
import com.transport.erp.auth.domain.RoleType;
import com.transport.erp.auth.domain.User;
import com.transport.erp.auth.dto.*;
import com.transport.erp.auth.repository.RoleRepository;
import com.transport.erp.auth.repository.UserRepository;
import com.transport.erp.auth.security.JwtTokenProvider;
import com.transport.erp.branch.domain.Branch;
import com.transport.erp.branch.repository.BranchRepository;
import com.transport.erp.common.exception.DuplicateResourceException;
import com.transport.erp.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final BranchRepository branchRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;

        public AuthResponse login(LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

                String accessToken = jwtTokenProvider.generateToken(authentication);
                String refreshToken = jwtTokenProvider.generateRefreshToken(request.getUsername());

                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username",
                                                request.getUsername()));

                Set<String> roles = user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet());

                Set<String> permissions = user.getRoles().stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(Enum::name)
                                .collect(Collectors.toSet());

                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .userId(user.getId())
                                .username(user.getUsername())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .roles(roles)
                                .permissions(permissions)
                                .build();
        }

        @Transactional
        public UserResponse register(RegisterRequest request) {
                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new DuplicateResourceException("User", "username", request.getUsername());
                }
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new DuplicateResourceException("User", "email", request.getEmail());
                }

                User user = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .email(request.getEmail())
                                .fullName(request.getFullName())
                                .phone(request.getPhone())
                                .active(true)
                                .build();

                // Set branch if provided
                if (request.getBranchId() != null) {
                        Branch branch = branchRepository.findById(request.getBranchId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Branch", "id",
                                                        request.getBranchId()));
                        user.setBranch(branch);
                }

                // Set roles
                Set<Role> roles = new HashSet<>();
                if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                        for (String roleName : request.getRoles()) {
                                RoleType roleType = RoleType.valueOf(roleName.toUpperCase());
                                Role role = roleRepository.findByName(roleType)
                                                .orElseThrow(() -> new ResourceNotFoundException("Role", "name",
                                                                roleName));
                                roles.add(role);
                        }
                } else {
                        // Default role is VIEWER
                        Role viewerRole = roleRepository.findByName(RoleType.VIEWER)
                                        .orElseThrow(() -> new ResourceNotFoundException("Role", "name", "VIEWER"));
                        roles.add(viewerRole);
                }
                user.setRoles(roles);

                User savedUser = userRepository.save(user);

                return mapToUserResponse(savedUser);
        }

        public AuthResponse refreshToken(String refreshToken) {
                if (!jwtTokenProvider.validateToken(refreshToken)) {
                        throw new IllegalArgumentException("Invalid refresh token");
                }

                String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
                String newAccessToken = jwtTokenProvider.generateTokenFromUsername(username);
                String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

                Set<String> roles = user.getRoles().stream()
                                .map(role -> role.getName().name())
                                .collect(Collectors.toSet());

                Set<String> permissions = user.getRoles().stream()
                                .flatMap(role -> role.getPermissions().stream())
                                .map(Enum::name)
                                .collect(Collectors.toSet());

                return AuthResponse.builder()
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken)
                                .tokenType("Bearer")
                                .userId(user.getId())
                                .username(user.getUsername())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .roles(roles)
                                .permissions(permissions)
                                .build();
        }

        @Transactional
        public UserResponse updateUser(java.util.UUID userId, UpdateUserRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

                // Update email if provided and not duplicate
                if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                        if (userRepository.existsByEmail(request.getEmail())) {
                                throw new DuplicateResourceException("User", "email", request.getEmail());
                        }
                        user.setEmail(request.getEmail());
                }

                // Update password if provided
                if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                }

                // Update basic fields if provided
                if (request.getFullName() != null) {
                        user.setFullName(request.getFullName());
                }
                if (request.getPhone() != null) {
                        user.setPhone(request.getPhone());
                }
                if (request.getActive() != null) {
                        user.setActive(request.getActive());
                }

                // Update branch if provided
                if (request.getBranchId() != null) {
                        Branch branch = branchRepository.findById(request.getBranchId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Branch", "id",
                                                        request.getBranchId()));
                        user.setBranch(branch);
                }

                // Update roles if provided
                if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                        Set<Role> roles = new HashSet<>();
                        for (String roleName : request.getRoles()) {
                                RoleType roleType = RoleType.valueOf(roleName.toUpperCase());
                                Role role = roleRepository.findByName(roleType)
                                                .orElseThrow(() -> new ResourceNotFoundException("Role", "name",
                                                                roleName));
                                roles.add(role);
                        }
                        user.setRoles(roles);
                }

                return mapToUserResponse(userRepository.save(user));
        }

        @Transactional(readOnly = true)
        public UserResponse getUserById(java.util.UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                return mapToUserResponse(user);
        }

        @Transactional(readOnly = true)
        public java.util.List<UserResponse> getAllUsers() {
                return userRepository.findAll().stream()
                                .map(this::mapToUserResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public void deactivateUser(java.util.UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                user.setActive(false);
                userRepository.save(user);
        }

        private UserResponse mapToUserResponse(User user) {
                return UserResponse.builder()
                                .id(user.getId())
                                .username(user.getUsername())
                                .email(user.getEmail())
                                .fullName(user.getFullName())
                                .phone(user.getPhone())
                                .active(user.isActive())
                                .branchId(user.getBranch() != null ? user.getBranch().getId() : null)
                                .branchName(user.getBranch() != null ? user.getBranch().getName() : null)
                                .roles(user.getRoles().stream()
                                                .map(role -> role.getName().name())
                                                .collect(Collectors.toSet()))
                                .build();
        }
}
