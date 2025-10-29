package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.service.AuthenticationService;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjeAyarlariService {

    private static final String MSG_PROJE_EKLEME_BASARILI = "proje.ekleme.basarili";
    private static final String MSG_PROJE_GUNCELLEME_BASARILI = "proje.guncelleme.basarili";
    private static final String MSG_PROJE_SILME_BASARILI = "proje.silme.basarili";
    private static final String MSG_PROJE_BULUNAMADI = "proje.bulunamadi";
    private static final String MSG_YETKISIZ_ERISIM = "yetkisiz.erisim";

    private final CompanyProjectRepository projectRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public SirketProjeAyarlaResponseDTO projeEkle(SirketProjeAyarlaRequestDTO requestDTO) {
        SirketProjeAyarlaResponseDTO responseDTO = new SirketProjeAyarlaResponseDTO();

        try {
            log.info("Proje ekleme işlemi başlatıldı: {}", requestDTO.getProjectName());

            User user = authenticationService.getAuthenticatedUser();
            log.debug("Kullanıcı doğrulandı: {}", user.getEmail());

            CompanyProject project = CompanyProject.builder()
                    .projectName(requestDTO.getProjectName())
                    .category(requestDTO.getCategory())
                    .location(requestDTO.getLocation())
                    .totalArea(requestDTO.getTotalArea())
                    .startDate(requestDTO.getStartDate())
                    .endDate(requestDTO.getEndDate())
                    .status(requestDTO.getStatus())
                    .durationMonths(requestDTO.getDurationMonths())
                    .description(requestDTO.getDescription())
                    .technicalSpecifications(requestDTO.getTechnicalSpecifications() != null ?
                            requestDTO.getTechnicalSpecifications() : new ArrayList<>())
                    .features(requestDTO.getFeatures() != null ?
                            requestDTO.getFeatures() : new ArrayList<>())
                    .user(user)
                    .build();

            CompanyProject savedProject = projectRepository.save(project);
            log.info("Proje kaydedildi: ID={}, Ad={}", savedProject.getId(), savedProject.getProjectName());

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_EKLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (ValidationServiceException | BusinessServiceException ex) {
            log.error("Proje ekleme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (Exception ex) {
            log.error("Beklenmeyen hata: {}", ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("SISTEM_HATASI", "Sistem hatası oluştu", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    @Transactional(readOnly = true)
    public SirketProjelerListeleResponseDTO projeleriListele() {
        SirketProjelerListeleResponseDTO responseDTO = new SirketProjelerListeleResponseDTO();

        try {
            User user = authenticationService.getAuthenticatedUser();

            List<CompanyProject> projects = projectRepository.findByUserAndDeletedFalse(user);
            log.info("Kullanıcının projeleri listelendi: {} adet", projects.size());

            if (projects.isEmpty()) {
                responseDTO.setMessages(List.of(
                        AppMessageUtil.createWithCode("Henüz proje eklenmemiş", AppMessageType.INFO)
                ));
                return responseDTO;
            }

            List<ProjeListDataResponseDTO> projeListesi = projects.stream()
                    .map(this::convertToProjeListDTO)
                    .toList();

            responseDTO.setData(projeListesi);

        } catch (BusinessServiceException ex) {
            log.error("Proje listeleme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    @Transactional
    public SirketProjeSilResponseDTO projeSoftDelete(Long projectId) {
        SirketProjeSilResponseDTO responseDTO = new SirketProjeSilResponseDTO();

        try {
            log.info("Proje silme işlemi başlatıldı: projectId={}", projectId);

            User user = authenticationService.getAuthenticatedUser();

            CompanyProject project = projectRepository.findByIdAndDeletedFalse(projectId)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI, "Proje bulunamadı veya zaten silinmiş"));

            if (!project.getUser().getId().equals(user.getId())) {
                throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Bu projeyi silme yetkiniz yok");
            }

            project.setDeleted(true);
            projectRepository.save(project);
            log.info("Proje silindi (soft delete): ID={}", projectId);

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_SILME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            log.error("Proje silme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ✅ PROJE GÜNCELLEME
    @Transactional
    public SirketProjeGuncelleResponseDTO projeGuncelle(Long projectId, SirketProjeGuncelleRequestDTO requestDTO) {
        SirketProjeGuncelleResponseDTO responseDTO = new SirketProjeGuncelleResponseDTO();

        try {
            log.info("Proje güncelleme işlemi başlatıldı: projectId={}", projectId);

            User user = authenticationService.getAuthenticatedUser();

            // Proje var mı ve silinmemiş mi kontrol et
            CompanyProject project = projectRepository.findByIdAndDeletedFalse(projectId)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI, "Proje bulunamadı veya silinmiş"));

            // Yetki kontrolü
            if (!project.getUser().getId().equals(user.getId())) {
                throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Bu projeyi güncelleme yetkiniz yok");
            }

            // Proje alanlarını güncelle
            updateProjectFields(project, requestDTO);

            CompanyProject updatedProject = projectRepository.save(project);
            log.info("Proje güncellendi: ID={}, Ad={}", updatedProject.getId(), updatedProject.getProjectName());

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_GUNCELLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            log.error("Proje güncelleme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (Exception ex) {
            log.error("Beklenmeyen hata: {}", ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("SISTEM_HATASI", "Sistem hatası oluştu", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ===== HELPER METHODS =====

    /**
     * Proje alanlarını günceller (null olmayan alanları)
     */
    private void updateProjectFields(CompanyProject project, SirketProjeGuncelleRequestDTO requestDTO) {
        if (requestDTO.getProjectName() != null && !requestDTO.getProjectName().isBlank()) {
            project.setProjectName(requestDTO.getProjectName());
        }
        if (requestDTO.getCategory() != null) {
            project.setCategory(requestDTO.getCategory());
        }
        if (requestDTO.getLocation() != null && !requestDTO.getLocation().isBlank()) {
            project.setLocation(requestDTO.getLocation());
        }
        if (requestDTO.getTotalArea() != null) {
            project.setTotalArea(requestDTO.getTotalArea());
        }
        if (requestDTO.getStartDate() != null) {
            project.setStartDate(requestDTO.getStartDate());
        }
        if (requestDTO.getEndDate() != null) {
            project.setEndDate(requestDTO.getEndDate());
        }
        if (requestDTO.getStatus() != null) {
            project.setStatus(requestDTO.getStatus());
        }
        if (requestDTO.getDurationMonths() != null) {
            project.setDurationMonths(requestDTO.getDurationMonths());
        }
        if (requestDTO.getDescription() != null && !requestDTO.getDescription().isBlank()) {
            project.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getTechnicalSpecifications() != null) {
            project.setTechnicalSpecifications(requestDTO.getTechnicalSpecifications());
        }
        if (requestDTO.getFeatures() != null) {
            project.setFeatures(requestDTO.getFeatures());
        }
    }

    private ProjeListDataResponseDTO convertToProjeListDTO(CompanyProject project) {
        ProjeListDataResponseDTO dto = new ProjeListDataResponseDTO();
        dto.setId(project.getId());
        dto.setProjectName(project.getProjectName());
        dto.setCategory(project.getCategory());
        dto.setLocation(project.getLocation());
        dto.setTotalArea(project.getTotalArea());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        dto.setStatus(project.getStatus());
        dto.setDurationMonths(project.getDurationMonths());
        dto.setDescription(project.getDescription());
        dto.setTechnicalSpecifications(project.getTechnicalSpecifications());
        dto.setFeatures(project.getFeatures());
        return dto;
    }
}