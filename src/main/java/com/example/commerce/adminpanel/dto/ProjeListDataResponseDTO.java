package com.example.commerce.adminpanel.dto;

import com.example.commerce.adminpanel.enums.ProjectCategory;
import com.example.commerce.adminpanel.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjeListDataResponseDTO{
    private Long id;
    private String uniqueCode;
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
    private List<ProjectFileMetaDTO> files; // ✅ Sadece meta data
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}