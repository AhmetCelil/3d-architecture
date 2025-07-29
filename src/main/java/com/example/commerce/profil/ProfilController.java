package com.example.commerce.profil;

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
    public ResponseEntity<SirketSifreGuncelleResponseDTO> urunSil(@RequestBody SirketSifreGuncelleRequestDTO requestDTO) {
        return ResponseEntity.ok(profilService.sirketSifreGuncelleResponseDTO(requestDTO));
    }
}
