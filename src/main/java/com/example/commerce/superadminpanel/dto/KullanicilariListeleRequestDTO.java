package com.example.commerce.superadminpanel.dto;

import com.example.commerce.auth.enums.Role;
import com.example.commerce.basedtos.PageRequestDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KullanicilariListeleRequestDTO extends PageRequestDto {
    /** null ise tüm şirketler, doluysa sadece o şirkete üye kullanıcılar. */
    private Long companyId;
    /** null ise tüm roller, doluysa sadece o role sahip kullanıcılar. */
    private Role role;
}
