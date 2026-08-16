package com.example.commerce.adminpanel.dto;

import com.example.commerce.adminpanel.enums.AnnouncementType;
import com.example.commerce.adminpanel.enums.DisplayFrequency;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuyuruDTO {
    private Long id;
    private String title;
    private String message;
    private AnnouncementType announcementType;
    private boolean hasImage;
    private String linkUrl;
    private String buttonText;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean active;
    private Integer priority;
    private DisplayFrequency displayFrequency;
    private LocalDateTime createdAt;
}
