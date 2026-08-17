package com.example.commerce.rent.entity;

import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.tenant.entity.Company;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "villa")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Villa extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(name = "description", length = 4000)
    private String description;

    private Integer capacity;

    @Column(precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "price_visible")
    @Builder.Default
    private boolean priceVisible = true;

    /** Bekleyen rezervasyonun tarihi otomatik serbest bırakana kadar geçecek süre. Null = hiç açılmasın (admin elle açar). */
    @Column(name = "hold_expiry_hours")
    private Integer holdExpiryHours;

    @Builder.Default
    private boolean active = true;

    @Builder.Default
    private boolean deleted = false;
}
