package com.example.commerce.publicapi.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PublicProjelerResponseDTO {
    private List<PublicProjeDTO> data;
    private boolean success;
    private String error;
}