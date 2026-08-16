package com.example.commerce.adminpanel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EkipUyesiKaydetRequestDTO {
    private String fullName;
    private String title;
    private String description;
    private Integer displayOrder;
    private Boolean active;
}
