package com.example.commerce.adminpanel.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IletisimMesajiDTO {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
