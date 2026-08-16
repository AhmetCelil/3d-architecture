package com.example.commerce.adminpanel.entity;

import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.tenant.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_profiles")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CompanyProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(name = "about_title")
    private String aboutTitle;

    @Column(name = "about_description")
    private String aboutDescription;

    private String mission;

    private String vision;

    @Column(name = "founded_year")
    private Integer foundedYear;

    private String story;

    @Column(name = "homepage_title")
    private String homepageTitle;

    @Column(name = "homepage_subtitle")
    private String homepageSubtitle;

    @Column(name = "completed_projects_count")
    private Integer completedProjectsCount;

    @Column(name = "ongoing_projects_count")
    private Integer ongoingProjectsCount;

    @Column(name = "experience_years")
    private Integer experienceYears;

    @Column(name = "happy_clients_count")
    private Integer happyClientsCount;
}
