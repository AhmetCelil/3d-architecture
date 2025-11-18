package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class PublicDosyaDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileData; // Base64
}