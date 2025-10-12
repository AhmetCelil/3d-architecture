package com.example.commerce.profil.controller;

import com.example.commerce.profil.dto.*;
import com.example.commerce.profil.service.ProfilServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sirket-profil")
@RequiredArgsConstructor
public class ProfilController {

    private final ProfilServiceImpl profilService;

    /**
     * Şirket şifresini günceller
     */
    @PreAuthorize("hasAuthority('SIRKET')")
    @PutMapping("/sifre-guncelle")
    public ResponseEntity<SirketSifreGuncelleResponseDTO> sirketSifreGuncelle(
            @RequestBody SirketSifreGuncelleRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketSifreGuncelle(requestDTO));
    }

    /**
     * Şirket profil bilgilerini ayarlar veya günceller
     */
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/profil-ayarla")
    public ResponseEntity<SirketProfilAyarlaResponseDTO> sirketProfilAyarla(
            @RequestBody SirketProfilAyarlaRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketProfilAyarla(requestDTO));
    }

    /**
     * Yeni proje ekler
     */
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping(value = "/proje-ekle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SirketProjeAyarlaResponseDTO> sirketProjeEkle(
            @ModelAttribute SirketProjeAyarlaRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketProjesiAyarla(requestDTO));
    }

    /**
     * Mevcut projeyi günceller
     */
    @PreAuthorize("hasAuthority('SIRKET')")
    @PutMapping(value = "/proje-guncelle/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SirketProjeGuncelleResponseDTO> sirketProjeGuncelle(
            @PathVariable Long projectId,
            @ModelAttribute SirketProjeGuncelleRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketProjesiGuncelle(projectId, requestDTO));
    }

    /**
     * Projeyi siler
     */
    @PreAuthorize("hasAuthority('SIRKET')")
    @DeleteMapping("/proje-sil/{projectId}")
    public ResponseEntity<SirketProjeSilResponseDTO> sirketProjeSil(@PathVariable Long projectId) {
        return ResponseEntity.ok(profilService.sirketProjesiSil(projectId));
    }

    /**
     * Kullanıcının tüm projelerini listeler
     */
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/projeler")
    public ResponseEntity<SirketProjelerListeleResponseDTO> sirketProjeleriniListele() {
        return ResponseEntity.ok(profilService.sirketProjeleriniListele());
    }
}
