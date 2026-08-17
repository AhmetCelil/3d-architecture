package com.example.commerce.rent.rentpublic.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicVillaDTO {
    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    /** priceVisible=false veya fiyat girilmemişse null döner; arayüz "iletişime geçin" göstermelidir. */
    private BigDecimal price;
    private boolean priceVisible;
    private List<PublicVillaImageDTO> images;
}
