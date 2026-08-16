package com.example.commerce.publicapi.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicIletisimResponseDTO extends BaseResponseDto {
    private PublicIletisimDTO data;
}
