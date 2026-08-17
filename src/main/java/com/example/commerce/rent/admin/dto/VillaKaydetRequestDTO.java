package com.example.commerce.rent.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VillaKaydetRequestDTO {
    private String name;
    private String description;
    private Integer capacity;
    private BigDecimal price;
    private Boolean priceVisible;
    /** Bekleyen rezervasyonun tarihi otomatik serbest bırakana kadar geçecek süre. Null = hiç açılmasın. */
    private Integer holdExpiryHours;
    private Boolean active;
}
