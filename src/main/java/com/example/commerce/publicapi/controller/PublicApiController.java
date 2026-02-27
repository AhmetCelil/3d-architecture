package com.example.commerce.publicapi.controller;

import com.example.commerce.publicapi.service.PublicProjeService;
import com.example.commerce.publicapi.dto.PublicDosyaResponseDTO;
import com.example.commerce.publicapi.dto.PublicProjeDetayResponseDTO;
import com.example.commerce.publicapi.dto.PublicProjelerResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Public API", description = "API key ile read-only erişim - Login gerektirmez")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final PublicProjeService publicProjeService;

    @Operation(summary = "API key ile kullanıcının projelerini listeler")
    @GetMapping("/projeler")
    public ResponseEntity<PublicProjelerResponseDTO> projeleriGetir(
            @RequestHeader("X-API-Key") String apiKey) {
        return ResponseEntity.ok(publicProjeService.projeleriGetir(apiKey));
    }

    @Operation(summary = "API key ile proje detayını getirir")
    @GetMapping("/proje/{projeId}")
    public ResponseEntity<PublicProjeDetayResponseDTO> projeDetayGetir(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long projeId) {
        return ResponseEntity.ok(publicProjeService.projeDetayGetir(apiKey, projeId));
    }

    @Operation(summary = "API key ile dosya indirir")
    @GetMapping("/proje/{projeId}/dosya/{dosyaId}")
    public ResponseEntity<PublicDosyaResponseDTO> dosyaIndir(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long projeId,
            @PathVariable Long dosyaId) {
        return ResponseEntity.ok(publicProjeService.dosyaIndir(apiKey, projeId, dosyaId));
    }

    @Operation(summary = "Unique code ile proje detayını getirir (herkes erişebilir)")
    @GetMapping("/proje/kod/{uniqueCode}")
    public ResponseEntity<PublicProjeDetayResponseDTO> projeDetayGetirByCode(
            @PathVariable String uniqueCode) {
        return ResponseEntity.ok(publicProjeService.projeDetayGetirByCode(uniqueCode));
    }
}