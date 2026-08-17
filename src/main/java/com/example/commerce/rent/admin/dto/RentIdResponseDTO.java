package com.example.commerce.rent.admin.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

/** rent modülünde yeni kayıt oluşturulunca id dönen ortak response. */
@Getter
@Setter
public class RentIdResponseDTO extends BaseResponseDto {
    private Long id;
}
