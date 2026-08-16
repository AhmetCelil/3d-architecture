package com.example.commerce.publicapi.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PublicCalismaSaatiDTO {
    private String dayOfWeek;
    private String opensAt;
    private String closesAt;
    private boolean closed;
}
