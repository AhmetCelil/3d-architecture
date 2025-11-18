package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class PublicProjeDTO {
    private Long id;
    private String uniqueCode;
    private String projectName;
    private String category;
    private String location;
    private Double totalArea;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private String description;
    private int dosyaSayisi;
}