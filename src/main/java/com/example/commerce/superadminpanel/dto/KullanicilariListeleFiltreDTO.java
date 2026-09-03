package com.example.commerce.superadminpanel.dto;

import com.example.commerce.auth.enums.Role;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KullanicilariListeleFiltreDTO {

    /** null ise tüm şirketler, doluysa sadece o şirkete üye kullanıcılar. */
    private Long companyId;
    /** null ise tüm roller, doluysa sadece o role sahip kullanıcılar. */
    private Role role;
}
