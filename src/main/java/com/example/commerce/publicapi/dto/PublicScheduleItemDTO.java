package com.example.commerce.publicapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Saha/takvim verisinin herkese açık güvenli alt kümesi: para veya kişisel veri içermez. */
@Getter
@Setter
@Builder
public class PublicScheduleItemDTO {
    private String name;
    private LocalDate plannedDate;
    private LocalDate actualDate;
    private String status;
    private Integer progressPercent;
}
