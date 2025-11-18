package com.example.commerce.publicapi.service;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.publicapi.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicProjeService {

    private final UserRepository userRepository;
    private final CompanyProjectRepository projectRepository;

    private User getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new BusinessServiceException("INVALID_API_KEY",
                        "Geçersiz API key"));
    }

    @Transactional
    public PublicProjelerResponseDTO projeleriGetir(String apiKey) {
        PublicProjelerResponseDTO responseDTO = new PublicProjelerResponseDTO();

        try {
            User user = getUserByApiKey(apiKey);

            List<CompanyProject> projects = projectRepository.findByUserAndDeletedFalse(user);

            log.info("Public API: Projeler listelendi - Email={}, Proje Sayısı={}",
                    user.getEmail(), projects.size());

            List<PublicProjeDTO> projeler = projects.stream()
                    .map(this::convertToPublicProjeDTO)
                    .toList();

            responseDTO.setData(projeler);
            responseDTO.setSuccess(true);

        } catch (BusinessServiceException ex) {
            log.error("Public API hatası: {}", ex.getMessage());
            responseDTO.setSuccess(false);
            responseDTO.setError(ex.getMessage());
        }

        return responseDTO;
    }

    @Transactional
    public PublicProjeDetayResponseDTO projeDetayGetir(String apiKey, Long projeId) {
        PublicProjeDetayResponseDTO responseDTO = new PublicProjeDetayResponseDTO();

        try {
            User user = getUserByApiKey(apiKey);

            CompanyProject project = projectRepository.findByIdAndUserAndDeletedFalse(projeId, user)
                    .orElseThrow(() -> new BusinessServiceException("PROJE_BULUNAMADI",
                            "Proje bulunamadı"));

            log.info("Public API: Proje detayı - Email={}, Proje ID={}",
                    user.getEmail(), projeId);

            PublicProjeDetayDTO detay = convertToPublicProjeDetayDTO(project);
            responseDTO.setData(detay);
            responseDTO.setSuccess(true);

        } catch (BusinessServiceException ex) {
            log.error("Public API hatası: {}", ex.getMessage());
            responseDTO.setSuccess(false);
            responseDTO.setError(ex.getMessage());
        }

        return responseDTO;
    }

    @Transactional
    public PublicDosyaResponseDTO dosyaIndir(String apiKey, Long projeId, Long dosyaId) {
        PublicDosyaResponseDTO responseDTO = new PublicDosyaResponseDTO();

        try {
            User user = getUserByApiKey(apiKey);

            CompanyProject project = projectRepository.findByIdAndUserAndDeletedFalse(projeId, user)
                    .orElseThrow(() -> new BusinessServiceException("PROJE_BULUNAMADI",
                            "Proje bulunamadı"));

            ProjectFile file = project.getFiles().stream()
                    .filter(f -> f.getId().equals(dosyaId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessServiceException("DOSYA_BULUNAMADI",
                            "Dosya bulunamadı"));

            log.info("Public API: Dosya indiriliyor - Email={}, Dosya={}",
                    user.getEmail(), file.getFileName());

            PublicDosyaDTO dosya = PublicDosyaDTO.builder()
                    .id(file.getId())
                    .fileName(file.getFileName())
                    .fileType(file.getFileType())
                    .fileSize(file.getFileSize())
                    .fileData(Base64.getEncoder().encodeToString(file.getFileData()))
                    .build();

            responseDTO.setData(dosya);
            responseDTO.setSuccess(true);

        } catch (BusinessServiceException ex) {
            log.error("Public API hatası: {}", ex.getMessage());
            responseDTO.setSuccess(false);
            responseDTO.setError(ex.getMessage());
        }

        return responseDTO;
    }

    @Transactional
    public PublicProjeDetayResponseDTO projeDetayGetirByCode(String uniqueCode) {
        PublicProjeDetayResponseDTO responseDTO = new PublicProjeDetayResponseDTO();

        try {
            CompanyProject project = projectRepository.findByUniqueCodeAndDeletedFalse(uniqueCode)
                    .orElseThrow(() -> new BusinessServiceException("PROJE_BULUNAMADI",
                            "Proje bulunamadı"));

            log.info("Public API: Proje detayı (unique code) - Kod={}", uniqueCode);

            PublicProjeDetayDTO detay = convertToPublicProjeDetayDTO(project);
            responseDTO.setData(detay);
            responseDTO.setSuccess(true);

        } catch (BusinessServiceException ex) {
            log.error("Public API hatası: {}", ex.getMessage());
            responseDTO.setSuccess(false);
            responseDTO.setError(ex.getMessage());
        }

        return responseDTO;
    }

    // Helpers
    private PublicProjeDTO convertToPublicProjeDTO(CompanyProject project) {
        return PublicProjeDTO.builder()
                .id(project.getId())
                .uniqueCode(project.getUniqueCode())
                .projectName(project.getProjectName())
                .category(project.getCategory().name())
                .location(project.getLocation())
                .totalArea(project.getTotalArea())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .description(project.getDescription())
                .dosyaSayisi(project.getFiles().size())
                .build();
    }

    private PublicProjeDetayDTO convertToPublicProjeDetayDTO(CompanyProject project) {
        return PublicProjeDetayDTO.builder()
                .id(project.getId())
                .uniqueCode(project.getUniqueCode())
                .projectName(project.getProjectName())
                .category(project.getCategory().name())
                .location(project.getLocation())
                .totalArea(project.getTotalArea())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus().name())
                .durationMonths(project.getDurationMonths())
                .description(project.getDescription())
                .technicalSpecifications(project.getTechnicalSpecifications())
                .features(project.getFeatures())
                .files(project.getFiles().stream()
                        .map(f -> PublicDosyaMetaDTO.builder()
                                .id(f.getId())
                                .fileName(f.getFileName())
                                .fileType(f.getFileType())
                                .fileSize(f.getFileSize())
                                .build())
                        .toList())
                .build();
    }
}