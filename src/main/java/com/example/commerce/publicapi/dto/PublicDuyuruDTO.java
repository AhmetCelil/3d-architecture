package com.example.commerce.publicapi.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicDuyuruDTO {
    private Long id;
    private String title;
    private String message;
    private String announcementType;
    private boolean hasImage;
    private String linkUrl;
    private String buttonText;
    private Integer priority;
    private String displayFrequency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
