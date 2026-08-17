package com.example.commerce.rent.entity;

import com.example.commerce.common.entity.BaseEntity;
import com.example.commerce.rent.enums.ReservationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "villa_reservation")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class VillaReservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "villa_id", nullable = false)
    private Villa villa;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "guest_surname")
    private String guestSurname;

    @Column(name = "guest_email", nullable = false)
    private String guestEmail;

    @Column(name = "guest_phone")
    private String guestPhone;

    @Column(name = "check_in", nullable = false)
    private LocalDate checkIn;

    @Column(name = "check_out", nullable = false)
    private LocalDate checkOut;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    /** Null = süresiz kilit (admin elle serbest bırakana kadar). */
    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;

    @Column(length = 2000)
    private String message;

    @Column(name = "kvkk_onay", nullable = false)
    private boolean kvkkOnay;

    @Column(name = "created_by_admin", nullable = false)
    @Builder.Default
    private boolean createdByAdmin = false;

    @Builder.Default
    private boolean deleted = false;
}
