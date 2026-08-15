package com.example.commerce.tenant.entity;

import com.example.commerce.auth.entity.User;
import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.tenant.enums.CompanyRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company_memberships", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "company_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class CompanyMembership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Enumerated(EnumType.STRING)
    @Column(name = "company_role", nullable = false)
    private CompanyRole companyRole;

    private boolean deleted = false;
}
