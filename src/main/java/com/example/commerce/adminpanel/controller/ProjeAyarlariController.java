package com.example.commerce.adminpanel.controller;

import com.example.commerce.adminpanel.dto.SirketProjeAyarlaRequestDTO;
import com.example.commerce.adminpanel.dto.SirketProjeAyarlaResponseDTO;
import com.example.commerce.adminpanel.dto.SirketProjeSilResponseDTO;
import com.example.commerce.adminpanel.dto.SirketProjelerListeleResponseDTO;
import com.example.commerce.adminpanel.service.ProjeAyarlariService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Şirket adminlerinin proje ekleme-çıkarma-silme-listeleme işlemi yapabilmesini sağlayan servisler")
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ProjeAyarlariController {

    private final ProjeAyarlariService profilService;

    @Operation(summary = "Şirket admininin yeni proje eklemesini sağlar")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping(value = "/proje-ekle")
    public ResponseEntity<SirketProjeAyarlaResponseDTO> sirketProjeEkle(
            @RequestBody SirketProjeAyarlaRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.projeEkle(requestDTO));
    }

    @Operation(summary = "Şirket admininin kendi eklediği projeleri listeler")
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/projeleri-listele")
    public ResponseEntity<SirketProjelerListeleResponseDTO> sirketProjeleriListele() {
        return ResponseEntity.ok(profilService.projeleriListele());
    }

    @Operation(summary = "Şirket admininin proje silmesini sağlar (soft delete)")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/proje-sil")
    public ResponseEntity<SirketProjeSilResponseDTO> projeSil(@RequestParam Long projectId) {
        return ResponseEntity.ok(profilService.projeSoftDelete(projectId));
    }
}