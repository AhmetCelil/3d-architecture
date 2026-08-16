package com.example.commerce.adminpanel.dto;

import com.example.commerce.adminpanel.enums.AnnouncementType;
import com.example.commerce.adminpanel.enums.DisplayFrequency;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DuyuruKaydetRequestDTO {
    private String title;
    private String message;
    private AnnouncementType announcementType;
    private String linkUrl;
    private String buttonText;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private Integer priority;
    private DisplayFrequency displayFrequency;
    /** Güncellemede true gönderilirse mevcut görsel kaldırılır (yeni görsel de yüklenmediyse). */
    private boolean removeImage;
}
