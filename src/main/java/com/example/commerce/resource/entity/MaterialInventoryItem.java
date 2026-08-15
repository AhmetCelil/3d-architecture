package com.example.commerce.resource.entity;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "material_inventory_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class MaterialInventoryItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private CompanyProject project;

    @Column(name = "material_name", nullable = false)
    private String materialName;

    @Column(nullable = false)
    private String unit;

    @Column(name = "quantity_planned", precision = 18, scale = 3)
    private BigDecimal quantityPlanned;

    @Column(name = "quantity_used", precision = 18, scale = 3)
    private BigDecimal quantityUsed;

    @Column(name = "quantity_remaining", precision = 18, scale = 3)
    private BigDecimal quantityRemaining;

    private String supplier;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
