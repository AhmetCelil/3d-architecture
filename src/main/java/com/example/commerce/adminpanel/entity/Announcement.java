package com.example.commerce.adminpanel.entity;

import com.example.commerce.adminpanel.enums.AnnouncementType;
import com.example.commerce.adminpanel.enums.DisplayFrequency;
import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.tenant.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Announcement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String title;

    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "announcement_type", nullable = false)
    private AnnouncementType announcementType;

    @Column(name = "image_data", columnDefinition = "bytea")
    private byte[] imageData;

    @Column(name = "image_file_name")
    private String imageFileName;

    @Column(name = "image_file_type")
    private String imageFileType;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "button_text")
    private String buttonText;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private Integer priority = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "display_frequency", nullable = false)
    @Builder.Default
    private DisplayFrequency displayFrequency = DisplayFrequency.HER_ZAMAN;

    @Builder.Default
    private boolean deleted = false;
}
