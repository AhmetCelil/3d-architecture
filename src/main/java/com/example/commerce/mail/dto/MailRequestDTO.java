package com.example.commerce.mail.dto;

import lombok.Data;

@Data
public class MailRequestDTO {
    private String isim;
    private String soyisim;
    private String telefon;
    private String email;
    private String aciklama;
}
