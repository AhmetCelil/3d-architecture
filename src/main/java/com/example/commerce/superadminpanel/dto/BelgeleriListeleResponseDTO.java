package com.example.commerce.superadminpanel.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BelgeleriListeleResponseDTO extends BaseResponseDto {
    private List<BelgeDTO> data;
}
