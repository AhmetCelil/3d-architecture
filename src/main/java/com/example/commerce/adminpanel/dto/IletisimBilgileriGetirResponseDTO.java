package com.example.commerce.adminpanel.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IletisimBilgileriGetirResponseDTO extends BaseResponseDto {
    private IletisimBilgileriDTO data;
}
