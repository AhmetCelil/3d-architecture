package com.example.commerce.publicapi.controller;

import com.example.commerce.publicapi.service.PublicProjeService;
import com.example.commerce.publicapi.service.PublicSiteService;
import com.example.commerce.publicapi.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Public API", description = "API key ile read-only erişim - Login gerektirmez")
@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicApiController {

    private final PublicProjeService publicProjeService;
    private final PublicSiteService publicSiteService;

    @Operation(summary = "API key ile kullanıcının projelerini listeler (sayfalı)")
    @GetMapping("/projeler")
    public ResponseEntity<PublicProjelerResponseDTO> projeleriGetir(
            @RequestHeader("X-API-Key") String apiKey,
            @ModelAttribute PublicProjeleriGetirRequestDTO request) {
        return ResponseEntity.ok(publicProjeService.projeleriGetir(apiKey, request));
    }

    @Operation(summary = "API key ile proje detayını getirir (sözleşme/bütçe, saha programı ve ekip dahil)")
    @GetMapping("/proje/{projeId}")
    public ResponseEntity<PublicProjeDetayResponseDTO> projeDetayGetir(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long projeId) {
        return ResponseEntity.ok(publicProjeService.projeDetayGetir(apiKey, projeId));
    }

    @Operation(summary = "Unique code ile proje detayını getirir (herkes erişebilir, finansal/kişisel veri içermez)")
    @GetMapping("/proje/kod/{uniqueCode}")
    public ResponseEntity<PublicProjeDetayResponseDTO> projeDetayGetirByCode(
            @PathVariable String uniqueCode) {
        return ResponseEntity.ok(publicProjeService.projeDetayGetirByCode(uniqueCode));
    }

    @Operation(summary = "API key ile projeye ait dosyayı (görsel vb.) tarayıcıda doğrudan görüntülenebilir şekilde döndürür")
    @GetMapping("/proje/{projeId}/dosya/{dosyaId}")
    public ResponseEntity<byte[]> dosyaGetir(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long projeId,
            @PathVariable Long dosyaId) {
        PublicProjeService.DownloadableFile file = publicProjeService.dosyaIndir(apiKey, projeId, dosyaId);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (file.fileType() != null && !file.fileType().isBlank()) {
            try {
                mediaType = MediaType.parseMediaType(file.fileType());
            } catch (IllegalArgumentException ignored) {
                // fileType beklenmeyen bir formatta ise octet-stream'e düş
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline().filename(file.fileName()).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(file.fileData());
    }

    @Operation(summary = "API key ile şirketin hakkımızda içeriğini (misyon/vizyon/değerler/neden biz/ekip) getirir")
    @GetMapping("/hakkimizda")
    public ResponseEntity<PublicHakkimizdaResponseDTO> hakkimizdaGetir(@RequestHeader("X-API-Key") String apiKey) {
        return ResponseEntity.ok(publicSiteService.hakkimizdaGetir(apiKey));
    }

    @Operation(summary = "API key ile şirketin iletişim bilgilerini (adres/telefon/sosyal medya/çalışma saatleri) getirir")
    @GetMapping("/iletisim")
    public ResponseEntity<PublicIletisimResponseDTO> iletisimGetir(@RequestHeader("X-API-Key") String apiKey) {
        return ResponseEntity.ok(publicSiteService.iletisimGetir(apiKey));
    }

    @Operation(summary = "API key ile ana sayfada gösterilecek tanıtım metni ve istatistikleri getirir")
    @GetMapping("/anasayfa")
    public ResponseEntity<PublicAnasayfaResponseDTO> anasayfaGetir(@RequestHeader("X-API-Key") String apiKey) {
        return ResponseEntity.ok(publicSiteService.anasayfaGetir(apiKey));
    }

    @Operation(summary = "API key ile aktif duyuruları (pop-up/banner/bar) getirir")
    @GetMapping("/duyurular")
    public ResponseEntity<PublicDuyurularResponseDTO> duyurulariGetir(@RequestHeader("X-API-Key") String apiKey) {
        return ResponseEntity.ok(publicSiteService.aktifDuyurulariGetir(apiKey));
    }

    @Operation(summary = "API key ile bir duyurunun görselini döndürür")
    @GetMapping("/duyuru/{duyuruId}/gorsel")
    public ResponseEntity<byte[]> duyuruGorseliGetir(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable Long duyuruId) {
        PublicSiteService.DuyuruGorsel gorsel = publicSiteService.duyuruGorseliGetir(apiKey, duyuruId);

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
                .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
                .body(gorsel.fileData());
    }

    @Operation(summary = "API key ile ziyaretçinin iletişim formu mesajını kaydeder")
    @PostMapping("/iletisim-formu")
    public ResponseEntity<PublicIletisimFormuResponseDTO> iletisimFormuGonder(
            @RequestHeader("X-API-Key") String apiKey,
            @RequestBody PublicIletisimFormuRequestDTO request) {
        return ResponseEntity.ok(publicSiteService.iletisimFormuGonder(apiKey, request));
    }
}