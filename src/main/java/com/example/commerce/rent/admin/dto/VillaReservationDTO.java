package com.example.commerce.rent.admin.dto;

import com.example.commerce.rent.enums.ReservationStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillaReservationDTO {
    private Long id;
    private Long villaId;
    private String villaName;
    private String guestName;
    private String guestSurname;
    private String guestEmail;
    private String guestPhone;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private ReservationStatus status;
    private LocalDateTime holdExpiresAt;
    private String message;
    private boolean createdByAdmin;
    private LocalDateTime createdAt;
}
