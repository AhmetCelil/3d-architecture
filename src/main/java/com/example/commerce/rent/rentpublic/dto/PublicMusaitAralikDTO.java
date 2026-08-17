package com.example.commerce.rent.rentpublic.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicMusaitAralikDTO {
    private LocalDate startDate;
    private LocalDate endDate;
}
