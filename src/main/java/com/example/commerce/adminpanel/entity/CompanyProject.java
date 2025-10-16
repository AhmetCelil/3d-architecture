package com.example.commerce.adminpanel.entity;

import com.example.commerce.auth.entity.User;
import com.example.commerce.adminpanel.enums.ProjectCategory;
import com.example.commerce.adminpanel.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "company_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompanyProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String projectName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectCategory category;

    @Column(nullable = false)
    private String location;

    @Column(name = "total_area")
    private Double totalArea;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Column(name = "duration_months")
    private Integer durationMonths;

    @Column(length = 2000)
    private String description;

    // GÖRSELLER (PNG, JPEG, PDF)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectImage> images = new ArrayList<>();

    // ✅ TEKNİK ÖZELLİKLER - LİSTE OLARAK
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_technical_specs", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "specification", length = 500)
    @Builder.Default
    private List<String> technicalSpecifications = new ArrayList<>();

    // PROJE ÖZELLİKLERİ
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_features", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "feature")
    @Builder.Default
    private List<String> features = new ArrayList<>();

    // KAT PLANLARI (PDF, PNG, JPEG)
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectFile> floorPlans = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public void addImage(ProjectImage image) {
        if (this.images == null) {
            this.images = new ArrayList<>();
        }
        this.images.add(image);
        image.setProject(this);
    }

    public void addFloorPlan(ProjectFile floorPlan) {
        if (this.floorPlans == null) {
            this.floorPlans = new ArrayList<>();
        }
        this.floorPlans.add(floorPlan);
        floorPlan.setProject(this);
    }

    @Override
    public String toString() {
        return "CompanyProject{" +
                "id=" + id +
                ", projectName='" + projectName + '\'' +
                ", category=" + category +
                ", location='" + location + '\'' +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyProject)) return false;
        CompanyProject that = (CompanyProject) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}