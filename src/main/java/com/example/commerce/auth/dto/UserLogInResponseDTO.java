package com.example.commerce.auth.dto;

import com.example.commerce.auth.enums.Role;
import com.example.commerce.basedtos.AppMessageDto;
import com.example.commerce.basedtos.BaseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLogInResponseDTO extends BaseResponseDto {
    private String token;
    private String refreshToken;
    private String email;
    private Role role;
    private List<AppMessageDto> messages;
}
