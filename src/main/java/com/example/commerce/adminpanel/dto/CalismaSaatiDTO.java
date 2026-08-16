package com.example.commerce.adminpanel.dto;

import com.example.commerce.adminpanel.enums.DayOfWeekTr;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalismaSaatiDTO {
    private DayOfWeekTr dayOfWeek;
    private String opensAt;
    private String closesAt;
    private boolean closed;
}
