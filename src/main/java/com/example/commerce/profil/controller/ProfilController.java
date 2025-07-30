package com.example.commerce.profil.controller;

import com.example.commerce.profil.dto.SirketProfilAyarlaRequestDTO;
import com.example.commerce.profil.dto.SirketProfilAyarlaResponseDTO;
import com.example.commerce.profil.service.ProfilServiceImpl;
import com.example.commerce.profil.dto.SirketSifreGuncelleRequestDTO;
import com.example.commerce.profil.dto.SirketSifreGuncelleResponseDTO;
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



}
