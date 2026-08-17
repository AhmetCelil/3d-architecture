package com.example.commerce.rent.admin.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillaImageDTO {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private Integer sortOrder;
}
