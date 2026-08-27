package com.example.commerce.rent.rentpublic.service;

import com.example.commerce.rent.rentpublic.dto.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface RentPublicService {

    PublicVillalarResponseDTO villalariListele(String apiKey);

    PublicVillalarResponseDTO musaitVillalariAra(String apiKey, LocalDate start, LocalDate end);

    PublicVillaDetayResponseDTO villaDetay(String apiKey, Long villaId);

    ResponseEntity<byte[]> gorselGetir(String apiKey, Long villaId, Long imageId);

    PublicMusaitlikResponseDTO musaitlik(String apiKey, Long villaId, LocalDate start, LocalDate end);

    PublicVillaFiyatResponseDTO fiyat(String apiKey, Long villaId, LocalDate start, LocalDate end);

    PublicVillaRezervasyonTalebiResponseDTO rezervasyonTalebiOlustur(String apiKey, Long villaId, PublicVillaRezervasyonTalebiRequestDTO request);
}
