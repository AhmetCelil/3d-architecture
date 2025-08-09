package com.example.commerce.profilayarlama.controller;

import com.example.commerce.profilayarlama.dto.*;
import com.example.commerce.profilayarlama.service.ProfilServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profil")
@AllArgsConstructor
public class ProfilController {

    private ProfilServiceImpl profilService;

    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/ekle")
    public ResponseEntity<String> urunEkle(@RequestParam String profilId) {
        return ResponseEntity.ok("Ürün başarıyla eklendi");
    }

    @PreAuthorize("hasAuthority('SIRKET')")
    @DeleteMapping("/sırket-sifre-guncelle")
    public ResponseEntity<SirketSifreGuncelleResponseDTO> sirketSifreGuncelle(@RequestBody SirketSifreGuncelleRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketSifreGuncelleResponseDTO(requestDTO));
    }

    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/sirket-profil-ayarla")
    public ResponseEntity<SirketProfilAyarlaResponseDTO> sirketProfilAyarla(@RequestBody SirketProfilAyarlaRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketProfilAyarla(requestDTO));
    }

    @PreAuthorize("hasAuthority('SIRKET')")
    @PostMapping("/sirket-proje-ayarla")
    public ResponseEntity<SirketProjeAyarlaResponseDTO> sirketProjeAyarla(@RequestBody SirketProjeAyarlaRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketProjesiAyarla(requestDTO));
    }


}
