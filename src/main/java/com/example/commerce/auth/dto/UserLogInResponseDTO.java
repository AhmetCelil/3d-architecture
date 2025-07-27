package com.example.commerce.auth.dto;

import com.example.commerce.auth.enums.Role;
import com.example.commerce.basedtos.BaseResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLogInResponseDTO {
    private String email;
    private Role role;
    private String token;
}
