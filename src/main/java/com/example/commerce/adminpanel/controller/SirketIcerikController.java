package com.example.commerce.adminpanel.controller;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.service.SirketIcerikService;
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

@Tag(name = "Şirket İçerik Yönetimi", description = "Hakkımızda, iletişim bilgileri ve duyuru yönetimi")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SIRKET')")
public class SirketIcerikController {

    private final SirketIcerikService icerikService;

    // --- Hakkımızda / Ana Sayfa ---

    @Operation(summary = "Hakkımızda ve ana sayfa içeriğini getirir")
    @GetMapping("/hakkimizda")
    public ResponseEntity<HakkimizdaGetirResponseDTO> hakkimizdaGetir() {
        return ResponseEntity.ok(icerikService.hakkimizdaGetir());
    }

    @Operation(summary = "Hakkımızda ve ana sayfa içeriğini günceller")
    @PutMapping("/hakkimizda")
    public ResponseEntity<SiteContentAckResponseDTO> hakkimizdaGuncelle(@RequestBody HakkimizdaGuncelleRequestDTO request) {
        return ResponseEntity.ok(icerikService.hakkimizdaGuncelle(request));
    }

    // --- Değerlerimiz ---

    @Operation(summary = "Şirket değerlerini listeler")
    @GetMapping("/hakkimizda/degerler")
    public ResponseEntity<DegerlerListeleResponseDTO> degerleriListele() {
        return ResponseEntity.ok(icerikService.degerleriListele());
    }

    @Operation(summary = "Yeni bir değer ekler")
    @PostMapping("/hakkimizda/degerler")
    public ResponseEntity<SiteContentIdResponseDTO> degerEkle(@RequestBody DegerKaydetRequestDTO request) {
        return ResponseEntity.ok(icerikService.degerEkle(request));
    }

    @Operation(summary = "Bir değeri günceller")
    @PutMapping("/hakkimizda/degerler/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> degerGuncelle(@PathVariable Long id, @RequestBody DegerKaydetRequestDTO request) {
        return ResponseEntity.ok(icerikService.degerGuncelle(id, request));
    }

    @Operation(summary = "Bir değeri siler")
    @DeleteMapping("/hakkimizda/degerler/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> degerSil(@PathVariable Long id) {
        return ResponseEntity.ok(icerikService.degerSil(id));
    }

    // --- Neden Bizi Seçmelisiniz ---

    @Operation(summary = "\"Neden bizi seçmelisiniz\" öğelerini listeler")
    @GetMapping("/hakkimizda/neden-biz")
    public ResponseEntity<NedenBizlerListeleResponseDTO> nedenBizleriListele() {
        return ResponseEntity.ok(icerikService.nedenBizleriListele());
    }

    @Operation(summary = "Yeni bir \"neden biz\" öğesi ekler")
    @PostMapping("/hakkimizda/neden-biz")
    public ResponseEntity<SiteContentIdResponseDTO> nedenBizEkle(@RequestBody NedenBizKaydetRequestDTO request) {
        return ResponseEntity.ok(icerikService.nedenBizEkle(request));
    }

    @Operation(summary = "Bir \"neden biz\" öğesini günceller")
    @PutMapping("/hakkimizda/neden-biz/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> nedenBizGuncelle(@PathVariable Long id, @RequestBody NedenBizKaydetRequestDTO request) {
        return ResponseEntity.ok(icerikService.nedenBizGuncelle(id, request));
    }

    @Operation(summary = "Bir \"neden biz\" öğesini siler")
    @DeleteMapping("/hakkimizda/neden-biz/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> nedenBizSil(@PathVariable Long id) {
        return ResponseEntity.ok(icerikService.nedenBizSil(id));
    }

    // --- Ekibimiz ---

    @Operation(summary = "Ekip üyelerini listeler")
    @GetMapping("/hakkimizda/ekip")
    public ResponseEntity<EkipUyeleriListeleResponseDTO> ekipUyeleriniListele() {
        return ResponseEntity.ok(icerikService.ekipUyeleriniListele());
    }

    @Operation(summary = "Yeni bir ekip üyesi ekler")
    @PostMapping("/hakkimizda/ekip")
    public ResponseEntity<SiteContentIdResponseDTO> ekipUyesiEkle(@RequestBody EkipUyesiKaydetRequestDTO request) {
        return ResponseEntity.ok(icerikService.ekipUyesiEkle(request));
    }

    @Operation(summary = "Bir ekip üyesini günceller")
    @PutMapping("/hakkimizda/ekip/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> ekipUyesiGuncelle(@PathVariable Long id, @RequestBody EkipUyesiKaydetRequestDTO request) {
        return ResponseEntity.ok(icerikService.ekipUyesiGuncelle(id, request));
    }

    @Operation(summary = "Bir ekip üyesini siler")
    @DeleteMapping("/hakkimizda/ekip/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> ekipUyesiSil(@PathVariable Long id) {
        return ResponseEntity.ok(icerikService.ekipUyesiSil(id));
    }

    // --- İletişim Bilgileri ---

    @Operation(summary = "İletişim bilgilerini (adres, telefon, sosyal medya, çalışma saatleri) getirir")
    @GetMapping("/iletisim-bilgileri")
    public ResponseEntity<IletisimBilgileriGetirResponseDTO> iletisimBilgileriGetir() {
        return ResponseEntity.ok(icerikService.iletisimBilgileriGetir());
    }

    @Operation(summary = "İletişim bilgilerini günceller")
    @PutMapping("/iletisim-bilgileri")
    public ResponseEntity<SiteContentAckResponseDTO> iletisimBilgileriGuncelle(@RequestBody IletisimBilgileriGuncelleRequestDTO request) {
        return ResponseEntity.ok(icerikService.iletisimBilgileriGuncelle(request));
    }

    // --- İletişim Mesajları (gelen kutusu) ---

    @Operation(summary = "Ziyaretçilerden gelen iletişim formu mesajlarını sayfalı listeler")
    @PostMapping("/iletisim-mesajlari")
    public ResponseEntity<IletisimMesajlariListeleResponseDTO> iletisimMesajlariniListele(@RequestBody IletisimMesajlariListeleRequestDTO request) {
        return ResponseEntity.ok(icerikService.iletisimMesajlariniListele(request));
    }

    @Operation(summary = "Bir mesajı okundu olarak işaretler")
    @PatchMapping("/iletisim-mesajlari/{id}/okundu")
    public ResponseEntity<SiteContentAckResponseDTO> iletisimMesajiOkunduIsaretle(@PathVariable Long id) {
        return ResponseEntity.ok(icerikService.iletisimMesajiOkunduIsaretle(id));
    }

    @Operation(summary = "Bir mesajı siler")
    @DeleteMapping("/iletisim-mesajlari/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> iletisimMesajiSil(@PathVariable Long id) {
        return ResponseEntity.ok(icerikService.iletisimMesajiSil(id));
    }

    // --- Duyuru / Pop-up ---

    @Operation(summary = "Şirket duyurularını (pop-up/banner/bar) listeler")
    @GetMapping("/duyurulari-listele")
    public ResponseEntity<DuyurulariListeleResponseDTO> duyurulariListele() {
        return ResponseEntity.ok(icerikService.duyurulariListele());
    }

    @Operation(summary = "Yeni bir duyuru ekler (opsiyonel görsel ile)")
    @PostMapping(value = "/duyuru-ekle", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SiteContentIdResponseDTO> duyuruEkle(
            @RequestPart("duyuruData") DuyuruKaydetRequestDTO requestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(icerikService.duyuruEkle(requestDTO, image));
    }

    @Operation(summary = "Bir duyuruyu günceller (opsiyonel yeni görsel ile)")
    @PutMapping(value = "/duyuru-guncelle/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SiteContentAckResponseDTO> duyuruGuncelle(
            @PathVariable Long id,
            @RequestPart("duyuruData") DuyuruKaydetRequestDTO requestDTO,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(icerikService.duyuruGuncelle(id, requestDTO, image));
    }

    @Operation(summary = "Bir duyuruyu siler")
    @DeleteMapping("/duyuru-sil/{id}")
    public ResponseEntity<SiteContentAckResponseDTO> duyuruSil(@PathVariable Long id) {
        return ResponseEntity.ok(icerikService.duyuruSil(id));
    }

    @Operation(summary = "Duyuruya ait görseli döndürür")
    @GetMapping("/duyuru/{id}/gorsel")
    public ResponseEntity<byte[]> duyuruGorseliGetir(@PathVariable Long id) {
        SirketIcerikService.DuyuruGorsel gorsel = icerikService.duyuruGorseliGetir(id);

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
}
