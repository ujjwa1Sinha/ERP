package com.transport.erp.branch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {

    private UUID id;
    private String name;
    private String code;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String phone;
    private String email;
    private String contactPerson;
    private boolean active;
    private Instant createdAt;
}
