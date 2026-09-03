package com.example.commerce.adminpanel.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UniteTipleriListeleResponseDTO extends BaseResponseDto {
    private List<UniteTipiDTO> data;
}
