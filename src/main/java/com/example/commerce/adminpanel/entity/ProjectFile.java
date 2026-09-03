package com.example.commerce.adminpanel.entity;

import com.example.commerce.adminpanel.enums.FileCategory;
import com.example.commerce.common.entity.BaseEntity;
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
@EqualsAndHashCode(callSuper = false)
public class ProjectFile extends BaseEntity {
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

    /** Bu dosya belirli bir ünite tipine (örn. "2+1 Tip A") ait bir kat planıysa dolu olur. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_type_id")
    private UnitType unitType;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_category")
    private FileCategory fileCategory;

    /** Kat planı görselleri için başlık, örn. "2+1 Tip A Kat Planı". */
    @Column(name = "title")
    private String title;

    /** Kat planı görseline bağlı oda/alan bazlı bilgiler (örn. Oturma Odası → 25 m²). */
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "project_file_room_details", joinColumns = @JoinColumn(name = "project_file_id"))
    @Builder.Default
    private List<FloorPlanRoomDetail> roomDetails = new ArrayList<>();

    private boolean deleted = false;
}
