package com.example.commerce.adminpanel.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SirketProjelerListeleResponseDTO extends BaseResponseDto {
    private List<ProjeDetayDTO> projeler;
}
