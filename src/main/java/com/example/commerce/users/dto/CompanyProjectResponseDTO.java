package com.example.commerce.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyProjectResponseDTO {
    private String projectName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileUrl; // MinIO pre-signed URL
}
