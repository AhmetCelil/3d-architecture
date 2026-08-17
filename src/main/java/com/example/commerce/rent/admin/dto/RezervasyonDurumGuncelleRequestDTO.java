package com.example.commerce.rent.admin.dto;

import com.example.commerce.rent.enums.ReservationStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RezervasyonDurumGuncelleRequestDTO {
    private ReservationStatus status;
}
