package com.example.commerce.rent.rentpublic.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicVillaDetayResponseDTO extends BaseResponseDto {
    private PublicVillaDTO data;
}
