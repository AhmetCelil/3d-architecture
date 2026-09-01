package com.example.commerce.superadminpanel.dto;

import com.example.commerce.auth.enums.Role;
import com.example.commerce.tenant.enums.CompanyRole;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UyeOzetDTO {
    private Long userId;
    private String email;
    private Role role;
    private CompanyRole companyRole;
}
