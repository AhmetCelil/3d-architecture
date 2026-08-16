package com.example.commerce.adminpanel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DegerKaydetRequestDTO {
    private String title;
    private String description;
    private String iconKey;
    private Integer displayOrder;
}
