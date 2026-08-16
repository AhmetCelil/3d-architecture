package com.example.commerce.adminpanel.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NedenBizKaydetRequestDTO {
    private String title;
    private String description;
    private String iconKey;
    private Integer displayOrder;
}
