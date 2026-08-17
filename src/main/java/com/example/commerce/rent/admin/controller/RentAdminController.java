package com.example.commerce.rent.admin.controller;

import com.example.commerce.rent.admin.dto.*;
import com.example.commerce.rent.admin.service.RentAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Villa Rezervasyon Yönetimi", description = "Villa, fiyat, müsaitlik ve rezervasyon yönetimi")
@RestController
@RequestMapping("/api/admin/rent")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SIRKET')")
public class RentAdminController {

    private final RentAdminService rentAdminService;

    // --- Villa ---

    @Operation(summary = "Şirkete ait villaları listeler")
    @GetMapping("/villalari-listele")
    public ResponseEntity<VillalariListeleResponseDTO> villalariListele() {
        return ResponseEntity.ok(rentAdminService.villalariListele());
    }

    @Operation(summary = "Yeni bir villa ekler")
    @PostMapping("/villa-ekle")
    public ResponseEntity<RentIdResponseDTO> villaEkle(@RequestBody VillaKaydetRequestDTO request) {
        return ResponseEntity.ok(rentAdminService.villaEkle(request));
    }

    @Operation(summary = "Bir villayı günceller")
    @PutMapping("/villa-guncelle/{id}")
    public ResponseEntity<RentAckResponseDTO> villaGuncelle(@PathVariable Long id, @RequestBody VillaKaydetRequestDTO request) {
        return ResponseEntity.ok(rentAdminService.villaGuncelle(id, request));
    }

    @Operation(summary = "Bir villayı siler")
    @DeleteMapping("/villa-sil/{id}")
    public ResponseEntity<RentAckResponseDTO> villaSil(@PathVariable Long id) {
        return ResponseEntity.ok(rentAdminService.villaSil(id));
    }

    // --- Galeri ---

    @Operation(summary = "Villaya bir veya birden çok görsel ekler")
    @PostMapping(value = "/villa/{villaId}/gorselleri-ekle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RentAckResponseDTO> gorselleriEkle(
            @PathVariable Long villaId,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.ok(rentAdminService.gorselleriEkle(villaId, images));
    }

    @Operation(summary = "Villaya ait bir görseli siler")
    @DeleteMapping("/villa/{villaId}/gorsel-sil/{imageId}")
    public ResponseEntity<RentAckResponseDTO> gorselSil(@PathVariable Long villaId, @PathVariable Long imageId) {
        return ResponseEntity.ok(rentAdminService.gorselSil(villaId, imageId));
    }

    @Operation(summary = "Villaya ait görseli döndürür")
    @GetMapping("/villa/{villaId}/gorsel/{imageId}")
    public ResponseEntity<byte[]> gorselGetir(@PathVariable Long villaId, @PathVariable Long imageId) {
        RentAdminService.VillaGorsel gorsel = rentAdminService.gorselGetir(villaId, imageId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (gorsel.fileType() != null && !gorsel.fileType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(gorsel.fileType());
            } catch (IllegalArgumentException ignored) {
                // fileType beklenmeyen bir formatta ise octet-stream'e düş
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(gorsel.fileName()).build().toString())
                .body(gorsel.fileData());
    }

    // --- Fiyat istisnaları ---

    @Operation(summary = "Villaya ait fiyat istisnalarını listeler")
    @GetMapping("/villa/{villaId}/fiyat-istisnalari-listele")
    public ResponseEntity<FiyatIstisnalariListeleResponseDTO> fiyatIstisnalariniListele(@PathVariable Long villaId) {
        return ResponseEntity.ok(rentAdminService.fiyatIstisnalariniListele(villaId));
    }

    @Operation(summary = "Villaya yeni bir fiyat istisnası ekler")
    @PostMapping("/villa/{villaId}/fiyat-istisnasi-ekle")
    public ResponseEntity<RentIdResponseDTO> fiyatIstisnasiEkle(@PathVariable Long villaId, @RequestBody VillaFiyatIstisnasiKaydetRequestDTO request) {
        return ResponseEntity.ok(rentAdminService.fiyatIstisnasiEkle(villaId, request));
    }

    @Operation(summary = "Bir fiyat istisnasını siler")
    @DeleteMapping("/villa/{villaId}/fiyat-istisnasi-sil/{id}")
    public ResponseEntity<RentAckResponseDTO> fiyatIstisnasiSil(@PathVariable Long villaId, @PathVariable Long id) {
        return ResponseEntity.ok(rentAdminService.fiyatIstisnasiSil(villaId, id));
    }

    // --- Müsaitlik blokları ---

    @Operation(summary = "Villaya ait manuel müsaitlik bloklarını listeler")
    @GetMapping("/villa/{villaId}/musaitlik-bloklari-listele")
    public ResponseEntity<MusaitlikBloklariListeleResponseDTO> musaitlikBloklariniListele(@PathVariable Long villaId) {
        return ResponseEntity.ok(rentAdminService.musaitlikBloklariniListele(villaId));
    }

    @Operation(summary = "Villaya yeni bir müsaitlik bloğu ekler")
    @PostMapping("/villa/{villaId}/musaitlik-blogu-ekle")
    public ResponseEntity<RentIdResponseDTO> musaitlikBlokuEkle(@PathVariable Long villaId, @RequestBody MusaitlikBlokuKaydetRequestDTO request) {
        return ResponseEntity.ok(rentAdminService.musaitlikBlokuEkle(villaId, request));
    }

    @Operation(summary = "Bir müsaitlik bloğunu siler")
    @DeleteMapping("/villa/{villaId}/musaitlik-blogu-sil/{id}")
    public ResponseEntity<RentAckResponseDTO> musaitlikBlokuSil(@PathVariable Long villaId, @PathVariable Long id) {
        return ResponseEntity.ok(rentAdminService.musaitlikBlokuSil(villaId, id));
    }

    // --- Rezervasyonlar ---

    @Operation(summary = "Şirketin tüm villalarındaki rezervasyonları listeler")
    @GetMapping("/rezervasyonlari-listele")
    public ResponseEntity<RezervasyonlariListeleResponseDTO> rezervasyonlariListele() {
        return ResponseEntity.ok(rentAdminService.rezervasyonlariListele());
    }

    @Operation(summary = "Admin'in telefon/elden anlaştığı bir rezervasyonu manuel olarak ekler")
    @PostMapping("/villa/{villaId}/rezervasyon-ekle")
    public ResponseEntity<RentIdResponseDTO> rezervasyonEkle(@PathVariable Long villaId, @RequestBody RezervasyonKaydetRequestDTO request) {
        return ResponseEntity.ok(rentAdminService.rezervasyonEkle(villaId, request));
    }

    @Operation(summary = "Bir rezervasyonun durumunu (onayla/reddet/iptal et) günceller")
    @PutMapping("/rezervasyon-durum-guncelle/{id}")
    public ResponseEntity<RentAckResponseDTO> rezervasyonDurumGuncelle(@PathVariable Long id, @RequestBody RezervasyonDurumGuncelleRequestDTO request) {
        return ResponseEntity.ok(rentAdminService.rezervasyonDurumGuncelle(id, request));
    }

    @Operation(summary = "Bekleyen bir rezervasyonun otomatik serbest kalma zamanını elle günceller")
    @PutMapping("/rezervasyon-hold-guncelle/{id}")
    public ResponseEntity<RentAckResponseDTO> rezervasyonHoldGuncelle(@PathVariable Long id, @RequestBody RezervasyonHoldGuncelleRequestDTO request) {
        return ResponseEntity.ok(rentAdminService.rezervasyonHoldGuncelle(id, request));
    }
}
