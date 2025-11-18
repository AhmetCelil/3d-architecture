package com.example.commerce.publicapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicDosyaResponseDTO {
    private PublicDosyaDTO data;
    private boolean success;
    private String error;
}