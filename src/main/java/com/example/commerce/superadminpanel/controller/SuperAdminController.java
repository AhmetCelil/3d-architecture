package com.example.commerce.superadminpanel.controller;

import com.example.commerce.superadminpanel.dto.*;
import com.example.commerce.superadminpanel.service.SuperAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Platform geneli süper admin paneli: tüm şirketleri (tenant) ve kullanıcıları
 * yönetmek, şirketlere belge yüklemek için. Sadece ADMIN yetkisi (RentPlatformAdminController
 * ile aynı platform-geneli yetki) erişebilir, şirketler kendi verileri dışına çıkamaz.
 */
@Tag(name = "Süper Admin Paneli")
@RestController
@RequestMapping("/api/admin/super-admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SuperAdminController {

    private final SuperAdminService superAdminService;

    // ---------------------------------------------------------------------
    // Şirketler
    // ---------------------------------------------------------------------

    @Operation(summary = "Tüm şirketleri sayfalı listeler (isimde arama opsiyonel)")
    @GetMapping("/companies")
    public ResponseEntity<SirketleriListeleResponseDTO> sirketleriListele(@ModelAttribute SirketleriListeleRequestDTO request) {
        return ResponseEntity.ok(superAdminService.sirketleriListele(request));
    }

    @Operation(summary = "Bir şirketin detayını (üyeler, rent modülü durumu dahil) getirir")
    @GetMapping("/companies/{companyId}")
    public ResponseEntity<SirketDetayGetirResponseDTO> sirketDetayGetir(@PathVariable Long companyId) {
        return ResponseEntity.ok(superAdminService.sirketDetayGetir(companyId));
    }

    @Operation(summary = "Bir şirketi aktif/pasif yapar")
    @PatchMapping("/companies/{companyId}/durum")
    public ResponseEntity<SirketDurumGuncelleResponseDTO> sirketDurumGuncelle(
            @PathVariable Long companyId, @RequestParam boolean active) {
        return ResponseEntity.ok(superAdminService.sirketDurumGuncelle(companyId, active));
    }

    // ---------------------------------------------------------------------
    // Kullanıcılar
    // ---------------------------------------------------------------------

    @Operation(summary = "Tüm kullanıcıları sayfalı listeler (şirket/rol filtresi opsiyonel)")
    @GetMapping("/users")
    public ResponseEntity<KullanicilariListeleResponseDTO> kullanicilariListele(@ModelAttribute KullanicilariListeleRequestDTO request) {
        return ResponseEntity.ok(superAdminService.kullanicilariListele(request));
    }

    @Operation(summary = "Bir kullanıcıyı siler (soft delete)")
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<KullaniciSilResponseDTO> kullaniciSil(@PathVariable Long userId) {
        return ResponseEntity.ok(superAdminService.kullaniciSil(userId));
    }

    // ---------------------------------------------------------------------
    // Şirket belgeleri
    // ---------------------------------------------------------------------

    @Operation(summary = "Bir şirket için belge yükler")
    @PostMapping(value = "/companies/{companyId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BelgeYukleResponseDTO> belgeYukle(
            @PathVariable Long companyId,
            @RequestParam(required = false) String title,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(superAdminService.belgeYukle(companyId, title, file));
    }

    @Operation(summary = "Bir şirketin belgelerini listeler (sadece metadata)")
    @GetMapping("/companies/{companyId}/documents")
    public ResponseEntity<BelgeleriListeleResponseDTO> belgeleriListele(@PathVariable Long companyId) {
        return ResponseEntity.ok(superAdminService.belgeleriListele(companyId));
    }

    @Operation(summary = "Bir belgeyi indirir")
    @GetMapping("/companies/{companyId}/documents/{belgeId}")
    public ResponseEntity<byte[]> belgeIndir(@PathVariable Long companyId, @PathVariable Long belgeId) {
        return superAdminService.belgeIndir(companyId, belgeId);
    }

    @Operation(summary = "Bir belgeyi siler (soft delete)")
    @DeleteMapping("/companies/{companyId}/documents/{belgeId}")
    public ResponseEntity<BelgeSilResponseDTO> belgeSil(@PathVariable Long companyId, @PathVariable Long belgeId) {
        return ResponseEntity.ok(superAdminService.belgeSil(companyId, belgeId));
    }
}
