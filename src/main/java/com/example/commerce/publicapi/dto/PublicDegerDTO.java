package com.example.commerce.publicapi.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicDegerDTO {
    private String title;
    private String description;
    private String iconKey;
}
