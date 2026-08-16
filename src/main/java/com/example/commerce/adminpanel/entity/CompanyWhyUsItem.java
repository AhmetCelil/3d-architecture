package com.example.commerce.adminpanel.entity;

import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.tenant.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_why_us_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CompanyWhyUsItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(name = "icon_key")
    private String iconKey;

    @Column(name = "display_order")
    @Builder.Default
    private Integer displayOrder = 0;

    @Builder.Default
    private boolean deleted = false;
}
