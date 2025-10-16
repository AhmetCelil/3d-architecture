package com.example.commerce.adminpanel.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType; // örn: image/png veya image/jpeg

    @Lob
    @Column(name = "image_data", columnDefinition = "BYTEA")
    private byte[] imageData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private CompanyProject project;

}
