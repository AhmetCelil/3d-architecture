package com.example.commerce.rent.rentpublic.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PublicVillalarResponseDTO extends BaseResponseDto {
    private List<PublicVillaDTO> data;
}
