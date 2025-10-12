package com.example.commerce.profil.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SirketProjeGuncelleResponseDTO extends BaseResponseDto {
    private Long projectId;
}
