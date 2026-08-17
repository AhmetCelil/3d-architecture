package com.example.commerce.rent.rentpublic.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/** Sorgulanan aralıkta villanın DOLU olduğu tarih aralıklarını döner (kalanlar müsaittir). */
@Getter
@Setter
public class PublicMusaitlikResponseDTO extends BaseResponseDto {
    private List<PublicMusaitAralikDTO> doluAraliklar;
}
