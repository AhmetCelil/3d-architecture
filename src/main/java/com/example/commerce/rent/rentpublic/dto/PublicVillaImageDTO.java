package com.example.commerce.rent.rentpublic.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicVillaImageDTO {
    private Long id;
    private String fileName;
    private String fileType;
}
