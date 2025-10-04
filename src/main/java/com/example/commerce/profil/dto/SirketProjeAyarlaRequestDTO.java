package com.example.commerce.profil.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SirketProjeAyarlaRequestDTO {
    private String projectName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
