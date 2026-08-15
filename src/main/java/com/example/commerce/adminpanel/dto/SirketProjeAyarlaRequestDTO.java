package com.example.commerce.adminpanel.dto;

import com.example.commerce.adminpanel.enums.ProjectCategory;
import com.example.commerce.adminpanel.enums.ProjectStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class SirketProjeAyarlaRequestDTO {
    private String projectName;
    private ProjectCategory category;
    private String location;
    private Double totalArea;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private Integer durationMonths;
    private String description;
    private List<String> technicalSpecifications;
    private List<String> features;

    /** "floorPlans" multipart dosya listesiyle index bazında eşleşir. */
    private List<FloorPlanInputDTO> floorPlanDetails;
}