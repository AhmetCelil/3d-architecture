package com.example.commerce.resource.entity;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "subcontractor_assignments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class SubcontractorAssignment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private CompanyProject project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subcontractor_id", nullable = false)
    private Subcontractor subcontractor;

    @Column(name = "scope_of_work", length = 2000)
    private String scopeOfWork;

    @Column(name = "contract_amount", precision = 18, scale = 2)
    private BigDecimal contractAmount;
}
