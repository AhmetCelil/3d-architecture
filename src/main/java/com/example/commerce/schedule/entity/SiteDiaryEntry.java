package com.example.commerce.schedule.entity;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.auth.entity.User;
import com.example.commerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "site_diary_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class SiteDiaryEntry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private CompanyProject project;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Column(name = "weather_condition")
    private String weatherCondition;

    @Column(name = "workforce_count")
    private Integer workforceCount;

    @Column(name = "work_description", length = 4000)
    private String workDescription;

    @Column(name = "issues_encountered", length = 2000)
    private String issuesEncountered;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by_user_id", nullable = false)
    private User reportedByUser;
}
