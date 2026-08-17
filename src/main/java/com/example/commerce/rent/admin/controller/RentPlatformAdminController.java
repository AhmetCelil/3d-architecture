package com.example.commerce.rent.admin.controller;

import com.example.commerce.rent.admin.dto.RentModulDurumDTO;
import com.example.commerce.rent.admin.service.RentPlatformAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Bir şirket için rent modülünü açıp kapatmak platform yetkisi (ADMIN) gerektirir, şirket kendi kendine açamaz. */
@Tag(name = "Rent Modülü Platform Yönetimi")
@RestController
@RequestMapping("/api/admin/rent/platform")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class RentPlatformAdminController {

    private final RentPlatformAdminService rentPlatformAdminService;

    @Operation(summary = "Bir şirket için rent modülünü açar/kapatır")
    @PatchMapping("/companies/{companyId}/durum")
    public ResponseEntity<RentModulDurumDTO> durumGuncelle(@PathVariable Long companyId, @RequestParam boolean enabled) {
        return ResponseEntity.ok(rentPlatformAdminService.durumGuncelle(companyId, enabled));
    }
}
