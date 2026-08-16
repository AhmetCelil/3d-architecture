package com.example.commerce.adminpanel.entity;

import com.example.commerce.adminpanel.enums.DayOfWeekTr;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkingHourEntry {

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeekTr dayOfWeek;

    @Column(name = "opens_at")
    private String opensAt;

    @Column(name = "closes_at")
    private String closesAt;

    private boolean closed;
}
