package com.example.commerce.superadminpanel.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdminSirketProjeEkleRequestDTO {
    private String companyEmail;

    private String projectName;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
