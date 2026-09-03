package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UniteTipiService {

    UniteTipiEkleResponseDTO uniteTipiEkle(
            Long projeId,
            UniteTipiInputDTO requestDTO,
            List<MultipartFile> floorPlans);

    UniteTipleriListeleResponseDTO uniteTipleriListele(Long projeId);

    UniteTipiDetayGetirResponseDTO uniteTipiDetayGetir(Long uniteTipiId);

    UniteTipiGuncelleResponseDTO uniteTipiGuncelle(
            Long uniteTipiId,
            UniteTipiInputDTO requestDTO,
            List<MultipartFile> newFloorPlans);

    UniteTipiSilResponseDTO uniteTipiSoftDelete(Long uniteTipiId);

    DosyaSilResponseDTO uniteTipiDosyaSil(Long uniteTipiId, Long dosyaId);
}
