package com.transport.erp.auth.service;

import com.transport.erp.auth.domain.RoleType;
import com.transport.erp.auth.domain.User;
import com.transport.erp.auth.dto.*;
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

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

        private final AuthenticationManager authenticationManager;
        private final UserRepository userRepository;
        private final BranchRepository branchRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtTokenProvider jwtTokenProvider;
        private final com.transport.erp.auth.security.SecurityService securityService;

        public AuthResponse login(LoginRequest request) {
                Authentication authentication = authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

                String accessToken = jwtTokenProvider.generateToken(authentication);
                String refreshToken = jwtTokenProvider.generateRefreshToken(request.getUsername());

                User user = userRepository.findByUsername(request.getUsername())
                                .orElseThrow(() -> new ResourceNotFoundException("User", "username",
                                                request.getUsername()));

                return buildAuthResponse(accessToken, refreshToken, user);
        }

        @Transactional
        public UserResponse register(RegisterRequest request) {
                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new DuplicateResourceException("User", "username", request.getUsername());
                }
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new DuplicateResourceException("User", "email", request.getEmail());
                }

                // Resolve role — default to VIEWER
                RoleType role = RoleType.VIEWER;
                if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                        String roleName = request.getRoles().iterator().next();
                        try {
                                role = RoleType.valueOf(roleName.toUpperCase());
                        } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("Unknown role: " + roleName);
                        }
                }

                User user = User.builder()
                                .username(request.getUsername())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .email(request.getEmail())
                                .fullName(request.getFullName())
                                .phone(request.getPhone())
                                .role(role)
                                .active(true)
                                .build();

                if (request.getBranchId() != null) {
                        Branch branch = branchRepository.findById(request.getBranchId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Branch", "id",
                                                        request.getBranchId()));
                        user.setBranch(branch);
                }

                return mapToUserResponse(userRepository.save(user));
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

                return buildAuthResponse(newAccessToken, newRefreshToken, user);
        }

        @Transactional
        public UserResponse updateUser(UUID userId, UpdateUserRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

                if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
                        if (userRepository.existsByEmail(request.getEmail())) {
                                throw new DuplicateResourceException("User", "email", request.getEmail());
                        }
                        user.setEmail(request.getEmail());
                }

                if (request.getPassword() != null && !request.getPassword().isBlank()) {
                        user.setPassword(passwordEncoder.encode(request.getPassword()));
                }
                if (request.getFullName() != null)
                        user.setFullName(request.getFullName());
                if (request.getPhone() != null)
                        user.setPhone(request.getPhone());
                if (request.getActive() != null)
                        user.setActive(request.getActive());

                if (request.getBranchId() != null) {
                        Branch branch = branchRepository.findById(request.getBranchId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Branch", "id",
                                                        request.getBranchId()));
                        user.setBranch(branch);
                }

                // Update role if provided (takes first entry)
                if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                        String roleName = request.getRoles().iterator().next();
                        try {
                                user.setRole(RoleType.valueOf(roleName.toUpperCase()));
                        } catch (IllegalArgumentException e) {
                                throw new IllegalArgumentException("Unknown role: " + roleName);
                        }
                }

                return mapToUserResponse(userRepository.save(user));
        }

        @Transactional(readOnly = true)
        public UserResponse getUserById(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                return mapToUserResponse(user);
        }

        @Transactional(readOnly = true)
        public List<UserResponse> getAllUsers() {
                com.transport.erp.auth.domain.User currentUser = securityService.getCurrentUser();
                java.util.stream.Stream<User> userStream;
                if (currentUser != null && currentUser.getRole() == com.transport.erp.auth.domain.RoleType.BRANCH_ADMIN
                                && currentUser.getBranch() != null) {
                        userStream = userRepository.findByBranchId(currentUser.getBranch().getId()).stream();
                } else {
                        userStream = userRepository.findAll().stream();
                }
                return userStream
                                .map(this::mapToUserResponse)
                                .collect(Collectors.toList());
        }

        @Transactional
        public void deactivateUser(UUID userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                user.setActive(false);
                userRepository.save(user);
        }

        // ── Private helpers ───────────────────────────────────────────────────────

        private AuthResponse buildAuthResponse(String accessToken, String refreshToken, User user) {
                return AuthResponse.builder()
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .tokenType("Bearer")
                                .userId(user.getId())
                                .username(user.getUsername())
                                .fullName(user.getFullName())
                                .email(user.getEmail())
                                .roles(Set.of(user.getRole().name()))
                                .permissions(user.getRole().permissions())
                                .build();
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
                                .roles(Set.of(user.getRole().name()))
                                .build();
        }
}
