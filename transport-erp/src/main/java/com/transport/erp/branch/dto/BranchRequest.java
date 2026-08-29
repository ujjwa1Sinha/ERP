package com.transport.erp.branch.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchRequest {

    @NotBlank(message = "Branch name is required")
    private String name;

    private String code;
    private String address;
    private String city;
    private String state;
    private String pinCode;
    private String phone;
    private String email;
    private String contactPerson;
}
