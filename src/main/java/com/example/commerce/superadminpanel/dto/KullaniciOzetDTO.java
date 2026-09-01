package com.example.commerce.superadminpanel.dto;

import com.example.commerce.auth.enums.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class KullaniciOzetDTO {
    private Long id;
    private String email;
    private Role role;
    private boolean enabled;
    private String companyName;
}
