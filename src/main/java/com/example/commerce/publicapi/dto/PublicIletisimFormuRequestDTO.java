package com.example.commerce.publicapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PublicIletisimFormuRequestDTO {
    private String fullName;
    private String email;
    private String phone;
    private String subject;
    private String message;
}
