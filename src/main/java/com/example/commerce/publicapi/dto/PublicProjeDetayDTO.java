package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class PublicProjeDetayDTO {
    private Long id;
    private String uniqueCode;
    private String projectName;
    private String category;
    private String location;
    private Double totalArea;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer durationMonths;
    private String description;
    private List<String> technicalSpecifications;
    private List<String> features;
    private List<PublicDosyaMetaDTO> files;
}