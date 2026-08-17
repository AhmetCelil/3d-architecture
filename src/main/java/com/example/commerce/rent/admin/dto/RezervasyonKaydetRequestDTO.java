package com.example.commerce.rent.admin.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Admin'in telefon/elden anlaştığı bir rezervasyonu manuel olarak sisteme girmesi için. */
@Getter
@Setter
public class RezervasyonKaydetRequestDTO {
    private String guestName;
    private String guestSurname;
    private String guestEmail;
    private String guestPhone;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String message;
}
