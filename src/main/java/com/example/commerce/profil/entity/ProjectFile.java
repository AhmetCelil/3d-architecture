package com.example.commerce.profil.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;
    private String fileType; // örn: application/pdf, image/png

    @Lob
    @Column(name = "file_data", columnDefinition = "BYTEA")
    private byte[] fileData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")
    private CompanyProject project;

    @Override
    public String toString() {
        return "ProjectFile{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + (fileData != null ? fileData.length : 0) + " bytes" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectFile)) return false;
        ProjectFile that = (ProjectFile) o;
        return id != null && id.equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}