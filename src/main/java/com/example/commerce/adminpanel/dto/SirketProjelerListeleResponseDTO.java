package com.example.commerce.adminpanel.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SirketProjelerListeleResponseDTO extends BaseResponseDto {
    @Schema(description = "Projeler")
    private List<ProjeListDataResponseDTO> data;
}
