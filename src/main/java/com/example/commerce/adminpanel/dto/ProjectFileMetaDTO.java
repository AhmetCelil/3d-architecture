package com.example.commerce.adminpanel.dto;

import com.example.commerce.adminpanel.enums.FileCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// Dosya meta verisi (liste için)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileMetaDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadDate;
    private FileCategory fileCategory;
    private List<String> projectFileDetails;
}