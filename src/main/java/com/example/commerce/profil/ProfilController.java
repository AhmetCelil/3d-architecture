package com.example.commerce.profil;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profil")
public class ProfilController {

    @PreAuthorize("hasAuthority('SATICI')")
    @PostMapping("/ekle")
    public ResponseEntity<String> urunEkle(@RequestParam String profilId) {
        return ResponseEntity.ok("Ürün başarıyla eklendi");
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/sil/{id}")
    public ResponseEntity<String> urunSil(@PathVariable Long id) {
        return ResponseEntity.ok("Ürün silindi");
    }
}
