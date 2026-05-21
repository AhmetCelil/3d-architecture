package com.example.commerce.adminpanel.dto;

import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.enums.FileCategory;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// Dosya detayı (indirme için)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFileDetailDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String fileData; // Base64
    private LocalDateTime uploadDate;
    private FileCategory fileCategory;
    private List<String> projectFileDetails;
}