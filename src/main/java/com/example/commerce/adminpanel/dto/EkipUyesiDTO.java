package com.example.commerce.adminpanel.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EkipUyesiDTO {
    private Long id;
    private String fullName;
    private String title;
    private String description;
    private Integer displayOrder;
    private boolean active;
}
