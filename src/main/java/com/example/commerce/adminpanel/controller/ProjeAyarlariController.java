package com.example.commerce.adminpanel.controller;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.service.ProjeAyarlariService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Şirket Proje Yönetimi", description = "Şirket adminlerinin proje ekleme-güncelleme-silme-listeleme işlemleri")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ProjeAyarlariController {

    private final ProjeAyarlariService projeService;

    @Operation(summary = "Şirket admininin yeni proje eklemesini sağlar")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/proje-ekle")
    public ResponseEntity<SirketProjeAyarlaResponseDTO> sirketProjeEkle(
            @RequestBody SirketProjeAyarlaRequestDTO requestDTO) {
        return ResponseEntity.ok(projeService.projeEkle(requestDTO));
    }

    @Operation(summary = "Şirket admininin kendi eklediği projeleri listeler")
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/projeleri-listele")
    public ResponseEntity<SirketProjelerListeleResponseDTO> sirketProjeleriListele() {
        return ResponseEntity.ok(projeService.projeleriListele());
    }

    @Operation(summary = "Şirket admininin proje günceller")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/proje-guncelle/{projectId}")
    public ResponseEntity<SirketProjeGuncelleResponseDTO> projeGuncelle(
            @PathVariable Long projectId,
            @RequestBody SirketProjeGuncelleRequestDTO requestDTO) {
        return ResponseEntity.ok(projeService.projeGuncelle(projectId, requestDTO));
    }

    @Operation(summary = "Şirket admininin proje silmesini sağlar (soft delete)")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/proje-sil/{projectId}")  // ✅ DELETE kullan, path variable
    public ResponseEntity<SirketProjeSilResponseDTO> projeSil(@PathVariable Long projectId) {
        return ResponseEntity.ok(projeService.projeSoftDelete(projectId));
    }
}