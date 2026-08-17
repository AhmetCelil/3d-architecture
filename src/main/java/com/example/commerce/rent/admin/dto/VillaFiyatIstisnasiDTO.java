package com.example.commerce.rent.admin.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillaFiyatIstisnasiDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;
    private String note;
}
