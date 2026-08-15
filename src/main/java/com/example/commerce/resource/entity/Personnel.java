package com.example.commerce.resource.entity;

import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.resource.enums.PersonnelType;
import com.example.commerce.tenant.entity.Company;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personnel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class Personnel extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    private String title;

    private String phone;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "personnel_type", nullable = false)
    private PersonnelType personnelType;

    @Builder.Default
    private boolean active = true;
}
