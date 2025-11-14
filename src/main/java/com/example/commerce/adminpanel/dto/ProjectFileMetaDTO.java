package com.example.commerce.adminpanel.dto;

import lombok.*;

import java.time.LocalDateTime;

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
}