package com.example.commerce.rent.rentpublic.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicGunlukFiyatDTO {
    private LocalDate date;
    private BigDecimal price;
    /** true ise bu gün için admin özel bir fiyat istisnası girmiştir, false ise villa'nın genel/sabit fiyatıdır. */
    private boolean specialPrice;
    private boolean available;
}
