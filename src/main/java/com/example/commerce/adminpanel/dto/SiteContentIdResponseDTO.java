package com.example.commerce.adminpanel.dto;

import com.example.commerce.basedtos.BaseResponseDto;
import lombok.Getter;
import lombok.Setter;

/** Hakkımızda/Duyuru modüllerinde yeni kayıt oluşturulunca id dönen ortak response. */
@Getter
@Setter
public class SiteContentIdResponseDTO extends BaseResponseDto {
    private Long id;
}
