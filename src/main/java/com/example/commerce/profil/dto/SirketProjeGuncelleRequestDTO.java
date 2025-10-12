package com.example.commerce.profil.dto;

import com.example.commerce.profil.enums.ProjectCategory;
import com.example.commerce.profil.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SirketProjeGuncelleRequestDTO {
    private String projectName;
    private ProjectCategory category;
    private String location;
    private Double totalArea;
    private LocalDate startDate;
    private LocalDate endDate;
    private ProjectStatus status;
    private Integer durationMonths;
    private String description;
    private String technicalSpecifications;
    private List<String> features;
    private List<MultipartFile> newImages;
    private List<MultipartFile> newFloorPlans;
    private List<Long> imageIdsToDelete;
    private List<Long> floorPlanIdsToDelete;
}