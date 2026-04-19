package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.ProjectFile;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjeAyarlariService {

    private static final String MSG_PROJE_EKLEME_BASARILI = "proje.ekleme.basarili";
    private static final String MSG_PROJE_GUNCELLEME_BASARILI = "proje.guncelleme.basarili";
    private static final String MSG_PROJE_SILME_BASARILI = "proje.silme.basarili";
    private static final String MSG_PROJE_BULUNAMADI = "proje.bulunamadi";
    public static final String MSG_DOSYA_BULUNAMADI = "DOSYA_BULUNAMADI";
    public static final String MSG_DOSYA_SILME_BASARILI = "DOSYA_SILME_BASARILI";

    private final CompanyProjectRepository projectRepository;
    private final AuthenticationService authenticationService;

    @Transactional
    public SirketProjeAyarlaResponseDTO projeEkle(SirketProjeAyarlaRequestDTO requestDTO, List<MultipartFile> files) {
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

            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {

                    ProjectFile projectFile = ProjectFile.builder()
                            .fileName(file.getOriginalFilename())
                            .fileType(file.getContentType())
                            .fileSize(file.getSize())
                            .fileData(file.getBytes())
                            .uploadDate(LocalDateTime.now())
                            .project(project) // ← burada ilişkiyi kur
                            .build();

                    project.getFiles().add(projectFile); // listeye ekle
                }
            }


            CompanyProject savedProject = projectRepository.save(project);
            log.info("Proje kaydedildi: ID={}, Ad={}, Dosya Sayısı={}",
                    savedProject.getId(), savedProject.getProjectName(), savedProject.getFiles().size());

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_EKLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (ValidationServiceException | BusinessServiceException ex) {
            log.error("Proje ekleme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (IOException ex) {
            log.error("Dosya işleme hatası: {}", ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("DOSYA_HATASI", "Dosya yüklenirken hata oluştu", AppMessageType.ERROR)
            ));
        } catch (Exception ex) {
            log.error("Beklenmeyen hata: {}", ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("SISTEM_HATASI", "Sistem hatası oluştu", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ✅ Listeleme (sadece meta data)
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

    // ✅ YENİ - Proje detayı getir (full data)
    @Transactional(readOnly = true)
    public ProjeDetayGetirResponseDTO projeDetayGetir(Long projeId) {
        ProjeDetayGetirResponseDTO responseDTO = new ProjeDetayGetirResponseDTO();

        try {
            User user = authenticationService.getAuthenticatedUser();

            CompanyProject project = projectRepository.findByIdAndUserAndDeletedFalse(projeId, user)
                    .orElseThrow(() -> new BusinessServiceException("","Proje bulunamadı veya erişim yetkiniz yok"));

            log.info("Proje detayı getiriliyor: ID={}", projeId);

            ProjeDetayResponseDTO detay = ProjeDetayResponseDTO.builder()
                    .id(project.getId())
                    .uniqueCode(project.getUniqueCode())
                    .projectName(project.getProjectName())
                    .category(project.getCategory())
                    .location(project.getLocation())
                    .totalArea(project.getTotalArea())
                    .startDate(project.getStartDate())
                    .endDate(project.getEndDate())
                    .status(project.getStatus())
                    .durationMonths(project.getDurationMonths())
                    .description(project.getDescription())
                    .technicalSpecifications(project.getTechnicalSpecifications())
                    .features(project.getFeatures())
                    .files(project.getFiles().stream()
                            .map(this::convertToFileDetailDTO)
                            .toList())
                    .build();

            responseDTO.setData(detay);

        } catch (BusinessServiceException ex) {
            log.error("Proje detay getirme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ✅ Service - User'ın dosyasını bul
    @Transactional(readOnly = true)
    public DosyaIndirResponseDTO dosyaIndir(Long dosyaId) {
        DosyaIndirResponseDTO responseDTO = new DosyaIndirResponseDTO();

        try {
            User user = authenticationService.getAuthenticatedUser();

            // User'ın projelerinden dosyayı bul
            ProjectFile file = user.getProjects().stream()
                    .filter(project -> !project.isDeleted()) // Silinmemiş projeler
                    .flatMap(project -> project.getFiles().stream())
                    .filter(f -> f.getId().equals(dosyaId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessServiceException("", "Dosya bulunamadı veya erişim yetkiniz yok"));

            log.info("Dosya indiriliyor: User={}, Dosya ID={}, Ad={}",
                    user.getEmail(), dosyaId, file.getFileName());

            ProjectFileDetailDTO detay = convertToFileDetailDTO(file);
            responseDTO.setData(detay);

        } catch (BusinessServiceException ex) {
            log.error("Dosya indirme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ✅ Helper - Liste için (meta data only)
    private ProjeListDataResponseDTO convertToProjeListDTO(CompanyProject project) {
        return ProjeListDataResponseDTO.builder()
                .id(project.getId())
                .uniqueCode(project.getUniqueCode())
                .projectName(project.getProjectName())
                .category(project.getCategory())
                .location(project.getLocation())
                .totalArea(project.getTotalArea())
                .startDate(project.getStartDate())
                .endDate(project.getEndDate())
                .status(project.getStatus())
                .durationMonths(project.getDurationMonths())
                .description(project.getDescription())
                .technicalSpecifications(project.getTechnicalSpecifications())
                .features(project.getFeatures())
                .files(project.getFiles().stream()
                        .map(this::convertToFileMetaDTO) // ✅ Sadece meta
                        .toList())
                .build();
    }

    // ✅ Helper - Meta data (byte[] yok)
    private ProjectFileMetaDTO convertToFileMetaDTO(ProjectFile file) {
        return ProjectFileMetaDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .uploadDate(file.getUploadDate())
                .build();
    }

    // ✅ Helper - Full data (byte[] -> base64)
    private ProjectFileDetailDTO convertToFileDetailDTO(ProjectFile file) {
        return ProjectFileDetailDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .fileData(Base64.getEncoder().encodeToString(file.getFileData())) // ✅ byte[] -> base64
                .uploadDate(file.getUploadDate())
                .build();
    }

    // ✅ Proje soft delete
    @Transactional
    public SirketProjeSilResponseDTO projeSoftDelete(Long projeId) {
        SirketProjeSilResponseDTO responseDTO = new SirketProjeSilResponseDTO();

        try {
            log.info("Proje silme işlemi başlatıldı: projeId={}", projeId);

            User user = authenticationService.getAuthenticatedUser();

            // User'a ait ve silinmemiş projeyi bul
            CompanyProject project = projectRepository.findByIdAndUserAndDeletedFalse(projeId, user)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI,
                            "Proje bulunamadı veya erişim yetkiniz yok"));

            // Soft delete
            project.setDeleted(true);
            projectRepository.save(project);

            log.info("Proje silindi (soft delete): ID={}, Kullanıcı={}", projeId, user.getEmail());

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

    // ✅ Dosya sil (hard delete - fiziksel silme)
    @Transactional
    public DosyaSilResponseDTO dosyaSil(Long projeId, Long dosyaId) {
        DosyaSilResponseDTO responseDTO = new DosyaSilResponseDTO();

        try {
            log.info("Dosya silme işlemi başlatıldı: projeId={}, dosyaId={}", projeId, dosyaId);

            User user = authenticationService.getAuthenticatedUser();

            // User'a ait projeyi bul
            CompanyProject project = projectRepository.findByIdAndUserAndDeletedFalse(projeId, user)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI,
                            "Proje bulunamadı veya erişim yetkiniz yok"));

            // Dosyayı bul
            ProjectFile fileToRemove = project.getFiles().stream()
                    .filter(f -> f.getId().equals(dosyaId))
                    .findFirst()
                    .orElseThrow(() -> new BusinessServiceException(MSG_DOSYA_BULUNAMADI,
                            "Dosya bulunamadı"));

            // Dosyayı projeden çıkar (orphanRemoval = true sayesinde DB'den silinir)
            project.getFiles().remove(fileToRemove);
            projectRepository.save(project);

            log.info("Dosya silindi: Proje ID={}, Dosya ID={}, Dosya Adı={}, Kullanıcı={}",
                    projeId, dosyaId, fileToRemove.getFileName(), user.getEmail());

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_DOSYA_SILME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            log.error("Dosya silme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ✅ PROJE GÜNCELLEME (Dosya ekleme ile)
    @Transactional
    public SirketProjeGuncelleResponseDTO projeGuncelle(Long projectId, SirketProjeGuncelleRequestDTO requestDTO, List<MultipartFile> files) {
        SirketProjeGuncelleResponseDTO responseDTO = new SirketProjeGuncelleResponseDTO();

        try {
            log.info("Proje güncelleme işlemi başlatıldı: projectId={}", projectId);

            User user = authenticationService.getAuthenticatedUser();

            // Kullanıcının projesi mi kontrol et
            CompanyProject project = projectRepository.findByIdAndUserAndDeletedFalse(projectId, user)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI,
                            "Proje bulunamadı veya erişim yetkiniz yok"));

            // Proje alanlarını güncelle
            updateProjectFields(project, requestDTO);

            // Yeni dosyalar varsa ekle
            if (files != null && !files.isEmpty()) {
                for (MultipartFile file : files) {

                    ProjectFile projectFile = ProjectFile.builder()
                            .fileName(file.getOriginalFilename())
                            .fileType(file.getContentType())
                            .fileSize(file.getSize())
                            .fileData(file.getBytes()) // byte[] olarak kaydet
                            .uploadDate(LocalDateTime.now())
                            .project(project)
                            .build();

                    project.getFiles().add(projectFile);
                }
                log.info("Projeye {} yeni dosya eklendi", files.size());
            }

            CompanyProject updatedProject = projectRepository.save(project);
            log.info("Proje güncellendi: ID={}, Ad={}, Toplam Dosya={}",
                    updatedProject.getId(), updatedProject.getProjectName(), updatedProject.getFiles().size());

            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_GUNCELLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (ValidationServiceException | BusinessServiceException ex) {
            log.error("Proje güncelleme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (IOException ex) {
            log.error("Dosya işleme hatası: {}", ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("DOSYA_HATASI", "Dosya yüklenirken hata oluştu", AppMessageType.ERROR)
            ));
        } catch (Exception ex) {
            log.error("Beklenmeyen hata: {}", ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("SISTEM_HATASI", "Sistem hatası oluştu", AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }


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
}