package com.example.commerce.rent.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class VillaFiyatIstisnasiKaydetRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private String note;
}
