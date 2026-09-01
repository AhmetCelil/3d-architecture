package com.example.commerce.superadminpanel.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BelgeDTO {
    private Long id;
    private String title;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private LocalDateTime uploadDate;
}
