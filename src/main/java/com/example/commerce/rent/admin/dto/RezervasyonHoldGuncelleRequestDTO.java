package com.example.commerce.rent.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Admin, bekleyen bir rezervasyonun otomatik serbest kalma zamanını elle değiştirebilir. Null = hiç açılmasın. */
@Getter
@Setter
public class RezervasyonHoldGuncelleRequestDTO {
    private LocalDateTime holdExpiresAt;
}
