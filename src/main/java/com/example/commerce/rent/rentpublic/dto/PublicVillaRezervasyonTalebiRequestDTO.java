package com.example.commerce.rent.rentpublic.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class PublicVillaRezervasyonTalebiRequestDTO {
    private String guestName;
    private String guestSurname;
    private String guestEmail;
    private String guestPhone;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String message;
    private boolean kvkkOnay;
    private String captchaToken;
}
