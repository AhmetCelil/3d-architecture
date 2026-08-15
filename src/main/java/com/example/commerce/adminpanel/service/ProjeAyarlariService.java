package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProjeAyarlariService {

    SirketProjeAyarlaResponseDTO projeEkle(
            SirketProjeAyarlaRequestDTO requestDTO,
            List<MultipartFile> images,
            List<MultipartFile> floorPlans);

    SirketProjelerListeleResponseDTO projeleriListele(ProjeleriListeleRequestDTO request);

    ProjeDetayGetirResponseDTO projeDetayGetir(Long projeId);

    DosyaIndirResponseDTO dosyaIndir(Long dosyaId);

    SirketProjeGuncelleResponseDTO projeGuncelle(
            Long projectId,
            SirketProjeGuncelleRequestDTO requestDTO,
            List<MultipartFile> newImages,
            List<MultipartFile> newFloorPlans);

    SirketProjeSilResponseDTO projeSoftDelete(Long projeId);

    DosyaSilResponseDTO dosyaSil(Long projeId, Long dosyaId);
}
