package com.example.commerce.rent.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MusaitlikBlokuKaydetRequestDTO {
    private LocalDate startDate;
    private LocalDate endDate;
    private String note;
}
