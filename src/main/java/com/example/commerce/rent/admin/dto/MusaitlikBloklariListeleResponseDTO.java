package com.example.commerce.rent.admin.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MusaitlikBloklariListeleResponseDTO extends BaseResponseDto {
    private List<MusaitlikBloguDTO> data;
}
