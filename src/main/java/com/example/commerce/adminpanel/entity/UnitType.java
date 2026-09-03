package com.example.commerce.adminpanel.entity;

import com.example.commerce.auth.entity.User;
import com.example.commerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Bir projeye bağlı daire/ünite konsepti (örn. "A Blok - 2+1 Bahçe Dubleks Tip A").
 * Blok ve konsept adlandırması firmadan firmaya çok değiştiği için serbest metin
 * tutulur; sadece filtrelenebilir alanlar (alan, oda sayısı) yapısal kolon olarak tutulur.
 */
@Entity
@Table(name = "unit_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class UnitType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private CompanyProject project;

    @Column(name = "block_label")
    private String blockLabel;

    @Column(nullable = false)
    private String label;

    private Double area;

    @Column(name = "room_count")
    private Integer roomCount;

    @Column(length = 2000)
    private String description;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "unit_type_room_details", joinColumns = @JoinColumn(name = "unit_type_id"))
    @Builder.Default
    private List<FloorPlanRoomDetail> roomDetails = new ArrayList<>();

    @OneToMany(mappedBy = "unitType", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProjectFile> files = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_user_id")
    private User updatedByUser;

    private boolean deleted = false;
}
