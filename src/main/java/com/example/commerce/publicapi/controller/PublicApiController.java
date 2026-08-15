package com.example.commerce.publicapi.controller;

import com.example.commerce.publicapi.service.PublicProjeService;
import com.example.commerce.publicapi.dto.PublicProjeDetayResponseDTO;
import com.example.commerce.publicapi.dto.PublicProjelerResponseDTO;
import com.example.commerce.publicapi.dto.PublicProjeleriGetirRequestDTO;
import com.example.commerce.publicapi.dto.PublicUnityBuildUrlDTO;
import com.example.commerce.publicapi.dto.PublicUnityBuildUrlResponseDTO;
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
}