package com.example.commerce.adminpanel.controller;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.service.ProjeAyarlariService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Şirket adminlerinin proje ekleme-çıkarma-silme-listeleme işlemi yapabilmesini sağlayan servisler")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ProjeAyarlariController {

    private final ProjeAyarlariService profilService;

    /**
     * Yeni proje ekler
     */
    @Operation(summary = "Şirket admininin yeni proje eklemesini sağlar")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping(value = "/proje-ekle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SirketProjeAyarlaResponseDTO> sirketProjeEkle(
            @ModelAttribute SirketProjeAyarlaRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.projeEkle(requestDTO));
    }

    /**
     * Mevcut projeyi günceller
     */
    @Operation(summary = "Şirket admininin projeyi güncellemesini sağlar")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PutMapping(value = "/proje-guncelle/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SirketProjeGuncelleResponseDTO> sirketProjeGuncelle(
            @PathVariable Long projectId,
            @ModelAttribute SirketProjeGuncelleRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.projeGuncelle(projectId, requestDTO));
    }

    /**
     * Projeyi siler
     */
    @Operation(summary = "Şirket admininin mevcut projeyi silmesini sağlar")
    @PreAuthorize("hasAuthority('SIRKET')")
    @DeleteMapping("/proje-sil/{projectId}")
    public ResponseEntity<SirketProjeSilResponseDTO> sirketProjeSil(@PathVariable Long projectId) {
        return ResponseEntity.ok(profilService.projeSil(projectId));
    }

    /**
     * Kullanıcının tüm projelerini listeler
     */
    @Operation(summary = "Şirket admininin projeleri listelemesini sağlar")
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/proje-listele")
    public ResponseEntity<SirketProjelerListeleResponseDTO> sirketProjeleriniListele() {
        return ResponseEntity.ok(profilService.projeListele());
    }
}
