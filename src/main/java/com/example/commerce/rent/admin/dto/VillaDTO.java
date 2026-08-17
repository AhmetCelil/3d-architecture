package com.example.commerce.rent.admin.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillaDTO {
    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    private BigDecimal price;
    private boolean priceVisible;
    private Integer holdExpiryHours;
    private boolean active;
    private List<VillaImageDTO> images;
}
