package com.example.commerce.adminpanel.dto;

import com.example.commerce.basedtos.AppMessageDto;
import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DosyaIndirResponseDTO extends BaseResponseDto {
    private ProjectFileDetailDTO data;
}