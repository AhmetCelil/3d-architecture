package com.example.commerce.rent.rentpublic.service;

import com.example.commerce.rent.rentpublic.dto.*;

import java.time.LocalDate;

public interface RentPublicService {

    PublicVillalarResponseDTO villalariListele(String apiKey);

    PublicVillaDetayResponseDTO villaDetay(String apiKey, Long villaId);

    VillaGorsel gorselGetir(String apiKey, Long villaId, Long imageId);

    PublicMusaitlikResponseDTO musaitlik(String apiKey, Long villaId, LocalDate start, LocalDate end);

    PublicVillaFiyatResponseDTO fiyat(String apiKey, Long villaId, LocalDate start, LocalDate end);

    PublicVillaRezervasyonTalebiResponseDTO rezervasyonTalebiOlustur(String apiKey, Long villaId, PublicVillaRezervasyonTalebiRequestDTO request);

    record VillaGorsel(String fileName, String fileType, byte[] fileData) {}
}
