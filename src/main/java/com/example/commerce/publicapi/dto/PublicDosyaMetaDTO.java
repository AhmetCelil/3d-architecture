package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PublicDosyaMetaDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
}