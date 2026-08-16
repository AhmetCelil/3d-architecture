package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface SirketIcerikService {

    // Hakkımızda / Ana Sayfa
    HakkimizdaGetirResponseDTO hakkimizdaGetir();
    SiteContentAckResponseDTO hakkimizdaGuncelle(HakkimizdaGuncelleRequestDTO request);

    // Değerlerimiz
    DegerlerListeleResponseDTO degerleriListele();
    SiteContentIdResponseDTO degerEkle(DegerKaydetRequestDTO request);
    SiteContentAckResponseDTO degerGuncelle(Long id, DegerKaydetRequestDTO request);
    SiteContentAckResponseDTO degerSil(Long id);

    // Neden Bizi Seçmelisiniz
    NedenBizlerListeleResponseDTO nedenBizleriListele();
    SiteContentIdResponseDTO nedenBizEkle(NedenBizKaydetRequestDTO request);
    SiteContentAckResponseDTO nedenBizGuncelle(Long id, NedenBizKaydetRequestDTO request);
    SiteContentAckResponseDTO nedenBizSil(Long id);

    // Ekibimiz
    EkipUyeleriListeleResponseDTO ekipUyeleriniListele();
    SiteContentIdResponseDTO ekipUyesiEkle(EkipUyesiKaydetRequestDTO request);
    SiteContentAckResponseDTO ekipUyesiGuncelle(Long id, EkipUyesiKaydetRequestDTO request);
    SiteContentAckResponseDTO ekipUyesiSil(Long id);

    // İletişim Bilgileri
    IletisimBilgileriGetirResponseDTO iletisimBilgileriGetir();
    SiteContentAckResponseDTO iletisimBilgileriGuncelle(IletisimBilgileriGuncelleRequestDTO request);

    // İletişim Mesajları (gelen kutusu)
    IletisimMesajlariListeleResponseDTO iletisimMesajlariniListele(IletisimMesajlariListeleRequestDTO request);
    SiteContentAckResponseDTO iletisimMesajiOkunduIsaretle(Long id);
    SiteContentAckResponseDTO iletisimMesajiSil(Long id);

    // Duyuru / Pop-up
    DuyurulariListeleResponseDTO duyurulariListele();
    SiteContentIdResponseDTO duyuruEkle(DuyuruKaydetRequestDTO request, MultipartFile image);
    SiteContentAckResponseDTO duyuruGuncelle(Long id, DuyuruKaydetRequestDTO request, MultipartFile image);
    SiteContentAckResponseDTO duyuruSil(Long id);

    record DuyuruGorsel(String fileName, String fileType, byte[] fileData) {}
    DuyuruGorsel duyuruGorseliGetir(Long id);
}
