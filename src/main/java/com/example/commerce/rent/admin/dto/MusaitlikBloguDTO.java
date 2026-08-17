package com.example.commerce.rent.admin.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MusaitlikBloguDTO {
    private Long id;
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
}
