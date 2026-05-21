package com.example.commerce.adminpanel.entity;

import com.example.commerce.adminpanel.enums.FileCategory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_data", columnDefinition = "bytea")
    private byte[] fileData;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "file_type")
    private String fileType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private CompanyProject project; // ← artık proje nesnesi üzerinden ilişki kuruluyor

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_category")
    private FileCategory fileCategory;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_file_details", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "project_file_details", length = 10000)
    @Builder.Default
    private List<String> projectFileDetails = new ArrayList<>();
}
