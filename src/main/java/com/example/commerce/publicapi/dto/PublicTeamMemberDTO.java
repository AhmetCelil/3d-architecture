package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Ekip üyesi (personel/taşeron). phone/email yalnızca API key ile firma
 * erişiminde doldurulur; unique-code ile herkese açık erişimde null bırakılır.
 */
@Getter
@Setter
@Builder
public class PublicTeamMemberDTO {
    private String name;
    private String role;
    private String phone;
    private String email;
}
