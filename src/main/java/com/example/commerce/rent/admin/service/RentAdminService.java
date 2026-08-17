package com.example.commerce.rent.admin.service;

import com.example.commerce.rent.admin.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface RentAdminService {

    // --- Villa ---
    VillalariListeleResponseDTO villalariListele();
    RentIdResponseDTO villaEkle(VillaKaydetRequestDTO request);
    RentAckResponseDTO villaGuncelle(Long id, VillaKaydetRequestDTO request);
    RentAckResponseDTO villaSil(Long id);

    // --- Galeri ---
    RentAckResponseDTO gorselleriEkle(Long villaId, List<MultipartFile> images);
    RentAckResponseDTO gorselSil(Long villaId, Long imageId);
    VillaGorsel gorselGetir(Long villaId, Long imageId);

    // --- Fiyat istisnaları ---
    FiyatIstisnalariListeleResponseDTO fiyatIstisnalariniListele(Long villaId);
    RentIdResponseDTO fiyatIstisnasiEkle(Long villaId, VillaFiyatIstisnasiKaydetRequestDTO request);
    RentAckResponseDTO fiyatIstisnasiSil(Long villaId, Long id);

    // --- Müsaitlik blokları (bakım vb. manuel kapatmalar) ---
    MusaitlikBloklariListeleResponseDTO musaitlikBloklariniListele(Long villaId);
    RentIdResponseDTO musaitlikBlokuEkle(Long villaId, MusaitlikBlokuKaydetRequestDTO request);
    RentAckResponseDTO musaitlikBlokuSil(Long villaId, Long id);

    // --- Rezervasyonlar ---
    RezervasyonlariListeleResponseDTO rezervasyonlariListele();
    RentIdResponseDTO rezervasyonEkle(Long villaId, RezervasyonKaydetRequestDTO request);
    RentAckResponseDTO rezervasyonDurumGuncelle(Long id, RezervasyonDurumGuncelleRequestDTO request);
    RentAckResponseDTO rezervasyonHoldGuncelle(Long id, RezervasyonHoldGuncelleRequestDTO request);

    record VillaGorsel(String fileName, String fileType, byte[] fileData) {}
}
