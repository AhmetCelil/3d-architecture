package com.example.commerce.rent.rentpublic.controller;

import com.example.commerce.rent.rentpublic.dto.*;
import com.example.commerce.rent.rentpublic.service.RentPublicService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Villa Rezervasyon (Public)", description = "Public villa listeleme, müsaitlik, fiyat ve rezervasyon talebi")
@RestController
@RequestMapping("/api/public/rent")
@RequiredArgsConstructor
public class RentPublicController {

    private final RentPublicService rentPublicService;

    @Operation(summary = "Aktif villaları listeler")
    @GetMapping("/villalari-listele")
    public ResponseEntity<PublicVillalarResponseDTO> villalariListele(@RequestHeader("X-API-Key") String apiKey) {
        return ResponseEntity.ok(rentPublicService.villalariListele(apiKey));
    }

    @Operation(summary = "Bir tarih aralığında müsait olan villaları listeler")
    @GetMapping("/villalari-ara")
    public ResponseEntity<PublicVillalarResponseDTO> musaitVillalariAra(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(rentPublicService.musaitVillalariAra(apiKey, start, end));
    }

    @Operation(summary = "Bir villanın detayını getirir")
    @GetMapping("/villa/{villaId}")
    public ResponseEntity<PublicVillaDetayResponseDTO> villaDetay(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long villaId) {
        return ResponseEntity.ok(rentPublicService.villaDetay(apiKey, villaId));
    }

    @Operation(summary = "Villaya ait görseli döndürür")
    @GetMapping("/villa/{villaId}/gorsel/{imageId}")
    public ResponseEntity<byte[]> gorselGetir(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long villaId,
            @PathVariable Long imageId) {
        return rentPublicService.gorselGetir(apiKey, villaId, imageId);
    }

    @Operation(summary = "Bir tarih aralığında villanın dolu olduğu aralıkları döner")
    @GetMapping("/villa/{villaId}/musaitlik")
    public ResponseEntity<PublicMusaitlikResponseDTO> musaitlik(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long villaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(rentPublicService.musaitlik(apiKey, villaId, start, end));
    }

    @Operation(summary = "Bir tarih aralığı için günlük fiyatları döner")
    @GetMapping("/villa/{villaId}/fiyat")
    public ResponseEntity<PublicVillaFiyatResponseDTO> fiyat(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long villaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {
        return ResponseEntity.ok(rentPublicService.fiyat(apiKey, villaId, start, end));
    }

    @Operation(summary = "Yeni bir rezervasyon talebi oluşturur (captcha + KVKK onayı zorunlu)")
    @PostMapping("/villa/{villaId}/rezervasyon-talebi-olustur")
    public ResponseEntity<PublicVillaRezervasyonTalebiResponseDTO> rezervasyonTalebiOlustur(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long villaId,
            @RequestBody PublicVillaRezervasyonTalebiRequestDTO request) {
        return ResponseEntity.ok(rentPublicService.rezervasyonTalebiOlustur(apiKey, villaId, request));
    }
}
