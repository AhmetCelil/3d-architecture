package com.example.commerce.publicapi.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PublicDuyurularResponseDTO extends BaseResponseDto {
    private List<PublicDuyuruDTO> data;
}
