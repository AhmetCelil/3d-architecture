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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Şirket Proje Yönetimi", description = "Şirket adminlerinin proje ekleme-güncelleme-silme-listeleme işlemleri")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class ProjeAyarlariController {

    private final ProjeAyarlariService projeService;

    @Operation(summary = "Şirket admininin yeni proje eklemesini sağlar")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping(value = "/proje-ekle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SirketProjeAyarlaResponseDTO> sirketProjeEkle(
            @RequestPart("projectData") SirketProjeAyarlaRequestDTO requestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart(value = "floorPlans", required = false) List<MultipartFile> floorPlans) {
        return ResponseEntity.ok(projeService.projeEkle(requestDTO, images, floorPlans));
    }

    @Operation(summary = "Şirket admininin kendi eklediği projeleri sayfalı olarak listeler (dosyalar sadece meta data)")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/projeleri-listele")
    public ResponseEntity<SirketProjelerListeleResponseDTO> sirketProjeleriListele(
            @RequestBody ProjeleriListeleRequestDTO request) {
        return ResponseEntity.ok(projeService.projeleriListele(request));
    }

    @Operation(summary = "Proje detayını dosyalarla birlikte getirir")
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/proje-detay-getir/{projeId}")
    public ResponseEntity<ProjeDetayGetirResponseDTO> projeDetayGetir(@PathVariable Long projeId) {
        return ResponseEntity.ok(projeService.projeDetayGetir(projeId));
    }

    @Operation(summary = "Kullanıcının projesine ait dosyayı base64 olarak indirir")
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/dosya/{dosyaId}")
    public ResponseEntity<DosyaIndirResponseDTO> dosyaIndir(@PathVariable Long dosyaId) {
        return ResponseEntity.ok(projeService.dosyaIndir(dosyaId));
    }

    @Operation(summary = "Şirket admininin proje günceller (resim ve kat planı ekleme ile)")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PutMapping(value = "/proje-guncelle/{projectId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SirketProjeGuncelleResponseDTO> projeGuncelle(
            @PathVariable Long projectId,
            @RequestPart("projectData") SirketProjeGuncelleRequestDTO requestDTO,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestPart(value = "newFloorPlans", required = false) List<MultipartFile> newFloorPlans) {
        return ResponseEntity.ok(projeService.projeGuncelle(projectId, requestDTO, newImages, newFloorPlans));
    }

    @Operation(summary = "Şirket admininin proje silmesini sağlar (soft delete)")
    @PreAuthorize("hasAuthority('SIRKET')")
    @DeleteMapping("/proje-sil/{projeId}")
    public ResponseEntity<SirketProjeSilResponseDTO> projeSil(@PathVariable Long projeId) {
        return ResponseEntity.ok(projeService.projeSoftDelete(projeId));
    }

    @Operation(summary = "Projedeki belirli bir dosyayı siler")
    @PreAuthorize("hasAuthority('SIRKET')")
    @DeleteMapping("/proje/{projeId}/dosya/{dosyaId}")
    public ResponseEntity<DosyaSilResponseDTO> dosyaSil(
            @PathVariable Long projeId,
            @PathVariable Long dosyaId) {
        return ResponseEntity.ok(projeService.dosyaSil(projeId, dosyaId));
    }

}