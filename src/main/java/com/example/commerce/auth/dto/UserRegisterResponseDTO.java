package com.example.commerce.auth.dto;

import com.example.commerce.auth.enums.Role;
import com.example.commerce.basedtos.BaseResponseDto;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegisterResponseDTO extends BaseResponseDto {
    private String email;
    private Role role;
    private String token;
}
