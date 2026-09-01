package com.example.commerce.superadminpanel.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SirketDurumGuncelleResponseDTO extends BaseResponseDto {
    private Long companyId;
    private boolean active;
}
