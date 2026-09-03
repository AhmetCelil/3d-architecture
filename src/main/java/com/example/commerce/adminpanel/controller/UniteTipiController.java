package com.example.commerce.adminpanel.controller;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.service.UniteTipiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Ünite Tipi Yönetimi", description = "Proje altında blok/konsept bazlı daire ünitesi ekleme-güncelleme-silme-listeleme işlemleri")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class UniteTipiController {

    private final UniteTipiService uniteTipiService;

    @Operation(summary = "Bir projeye yeni ünite tipi (blok/konsept) ekler")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping(value = "/proje/{projeId}/unite-tipi-ekle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UniteTipiEkleResponseDTO> uniteTipiEkle(
            @PathVariable Long projeId,
            @RequestPart("uniteData") UniteTipiInputDTO requestDTO,
            @RequestPart(value = "floorPlans", required = false) List<MultipartFile> floorPlans) {
        return ResponseEntity.ok(uniteTipiService.uniteTipiEkle(projeId, requestDTO, floorPlans));
    }

    @Operation(summary = "Bir projeye ait ünite tiplerini listeler")
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/proje/{projeId}/unite-tipleri-listele")
    public ResponseEntity<UniteTipleriListeleResponseDTO> uniteTipleriListele(@PathVariable Long projeId) {
        return ResponseEntity.ok(uniteTipiService.uniteTipleriListele(projeId));
    }

    @Operation(summary = "Ünite tipi detayını dosyalarıyla birlikte getirir")
    @PreAuthorize("hasAuthority('SIRKET')")
    @GetMapping("/unite-tipi-detay-getir/{uniteTipiId}")
    public ResponseEntity<UniteTipiDetayGetirResponseDTO> uniteTipiDetayGetir(@PathVariable Long uniteTipiId) {
        return ResponseEntity.ok(uniteTipiService.uniteTipiDetayGetir(uniteTipiId));
    }

    @Operation(summary = "Ünite tipini günceller (yeni kat planı ekleme ile)")
    @PreAuthorize("hasAuthority('SIRKET')")
    @PutMapping(value = "/unite-tipi-guncelle/{uniteTipiId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UniteTipiGuncelleResponseDTO> uniteTipiGuncelle(
            @PathVariable Long uniteTipiId,
            @RequestPart("uniteData") UniteTipiInputDTO requestDTO,
            @RequestPart(value = "newFloorPlans", required = false) List<MultipartFile> newFloorPlans) {
        return ResponseEntity.ok(uniteTipiService.uniteTipiGuncelle(uniteTipiId, requestDTO, newFloorPlans));
    }

    @Operation(summary = "Ünite tipini siler (soft delete)")
    @PreAuthorize("hasAuthority('SIRKET')")
    @DeleteMapping("/unite-tipi-sil/{uniteTipiId}")
    public ResponseEntity<UniteTipiSilResponseDTO> uniteTipiSil(@PathVariable Long uniteTipiId) {
        return ResponseEntity.ok(uniteTipiService.uniteTipiSoftDelete(uniteTipiId));
    }

    @Operation(summary = "Ünite tipine ait belirli bir kat planı dosyasını siler")
    @PreAuthorize("hasAuthority('SIRKET')")
    @DeleteMapping("/unite-tipi/{uniteTipiId}/dosya/{dosyaId}")
    public ResponseEntity<DosyaSilResponseDTO> uniteTipiDosyaSil(
            @PathVariable Long uniteTipiId,
            @PathVariable Long dosyaId) {
        return ResponseEntity.ok(uniteTipiService.uniteTipiDosyaSil(uniteTipiId, dosyaId));
    }
}
