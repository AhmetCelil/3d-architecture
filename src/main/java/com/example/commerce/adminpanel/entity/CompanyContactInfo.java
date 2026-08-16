package com.example.commerce.adminpanel.entity;

import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.tenant.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company_contact_infos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CompanyContactInfo extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    private String address;

    private String phone;

    @Column(name = "phone_secondary")
    private String phoneSecondary;

    private String email;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    @Column(name = "instagram_url")
    private String instagramUrl;

    @Column(name = "twitter_url")
    private String twitterUrl;

    @Column(name = "facebook_url")
    private String facebookUrl;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "youtube_url")
    private String youtubeUrl;

    @Column(name = "map_latitude")
    private BigDecimal mapLatitude;

    @Column(name = "map_longitude")
    private BigDecimal mapLongitude;

    @Column(name = "map_embed_url")
    private String mapEmbedUrl;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "company_working_hours", joinColumns = @JoinColumn(name = "contact_info_id"))
    @Builder.Default
    private List<WorkingHourEntry> workingHours = new ArrayList<>();
}
