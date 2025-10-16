package com.example.commerce.adminpanel.service;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.entity.ProjectImage;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjeAyarlariService {

    private static final String MSG_PROJE_EKLEME_BASARILI = "proje.ekleme.basarili";
    private static final String MSG_PROJE_GUNCELLEME_BASARILI = "proje.guncelleme.basarili";
    private static final String MSG_PROJE_SILME_BASARILI = "proje.silme.basarili";
    private static final String MSG_KULLANICI_BULUNAMADI = "kullanici.bulunamadi";
    private static final String MSG_PROJE_BULUNAMADI = "proje.bulunamadi";
    private static final String MSG_YETKISIZ_ERISIM = "yetkisiz.erisim";
    private static final String MSG_DOSYA_YUKLEME_HATASI = "dosya.yukleme.hatasi";
    private static final String MSG_GECERSIZ_DOSYA_FORMATI = "gecersiz.dosya.formati";

    // İZİN VERİLEN DOSYA TİPLERİ
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final List<String> ALLOWED_FLOORPLAN_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private final UserRepository userRepository;
    private final CompanyProjectRepository projectRepository;

    @Transactional
    public SirketProjeAyarlaResponseDTO projeEkle(SirketProjeAyarlaRequestDTO requestDTO) {
        SirketProjeAyarlaResponseDTO responseDTO = new SirketProjeAyarlaResponseDTO();

        try {
            log.info("Proje ekleme işlemi başlatıldı: {}", requestDTO.getProjectName());

            User user = getAuthenticatedUser();
            log.debug("Kullanıcı doğrulandı: {}", user.getEmail());

            // Zorunlu alan validasyonları
            validateProjectRequest(requestDTO);

            // Dosya formatı validasyonları
            if (requestDTO.getImages() != null && !requestDTO.getImages().isEmpty()) {
                log.debug("Görsel dosyaları kontrol ediliyor: {} adet", requestDTO.getImages().size());
                validateFiles(requestDTO.getImages(), ALLOWED_IMAGE_TYPES, "Görsel");
            }
            if (requestDTO.getFloorPlans() != null && !requestDTO.getFloorPlans().isEmpty()) {
                log.debug("Kat planı dosyaları kontrol ediliyor: {} adet", requestDTO.getFloorPlans().size());
                validateFiles(requestDTO.getFloorPlans(), ALLOWED_FLOORPLAN_TYPES, "Kat planı");
            }

            // Proje oluştur - ÖNCE İLİŞKİLER OLMADAN
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
                    .images(new ArrayList<>())
                    .floorPlans(new ArrayList<>())
                    .build();

            log.debug("Proje entity oluşturuldu");

            // ✅ ÖNCELİKLE PROJEYİ KAYDET (ID alması için)
            CompanyProject savedProject = projectRepository.save(project);
            log.info("Proje kaydedildi: ID={}, Ad={}", savedProject.getId(), savedProject.getProjectName());

            // ✅ ŞIMDI GÖRSELLERI EKLE VE KAYDET
            if (requestDTO.getImages() != null && !requestDTO.getImages().isEmpty()) {
                for (MultipartFile file : requestDTO.getImages()) {
                    if (!file.isEmpty()) {
                        try {
                            ProjectImage image = ProjectImage.builder()
                                    .fileName(file.getOriginalFilename())
                                    .fileType(file.getContentType())
                                    .imageData(file.getBytes())
                                    .project(savedProject)  // ✅ Kaydedilmiş projeyi kullan
                                    .build();
                            savedProject.getImages().add(image);
                            log.debug("Görsel eklendi: {}", file.getOriginalFilename());
                        } catch (IOException e) {
                            log.error("Görsel yüklenirken hata: {}", file.getOriginalFilename(), e);
                            throw new BusinessServiceException(MSG_DOSYA_YUKLEME_HATASI,
                                    "Görsel yüklenirken hata oluştu: " + file.getOriginalFilename());
                        }
                    }
                }
            }

            // ✅ KAT PLANLARINI EKLE
            if (requestDTO.getFloorPlans() != null && !requestDTO.getFloorPlans().isEmpty()) {
                for (MultipartFile file : requestDTO.getFloorPlans()) {
                    if (!file.isEmpty()) {
                        try {
                            ProjectFile floorPlan = ProjectFile.builder()
                                    .fileName(file.getOriginalFilename())
                                    .fileType(file.getContentType())
                                    .fileData(file.getBytes())
                                    .project(savedProject)  // ✅ Kaydedilmiş projeyi kullan
                                    .build();
                            savedProject.getFloorPlans().add(floorPlan);
                            log.debug("Kat planı eklendi: {}", file.getOriginalFilename());
                        } catch (IOException e) {
                            log.error("Kat planı yüklenirken hata: {}", file.getOriginalFilename(), e);
                            throw new BusinessServiceException(MSG_DOSYA_YUKLEME_HATASI,
                                    "Kat planı yüklenirken hata oluştu: " + file.getOriginalFilename());
                        }
                    }
                }
            }

            // ✅ DOSYALARLA BİRLİKTE SON KEZ KAYDET
            if (!savedProject.getImages().isEmpty() || !savedProject.getFloorPlans().isEmpty()) {
                savedProject = projectRepository.save(savedProject);
                log.debug("Proje dosyalarla birlikte güncellendi");
            }

            // Kullanıcıya proje ilişkisini ekle (gerekirse)
            if (user.getProjects() == null) {
                user.setProjects(new ArrayList<>());
            }
            if (!user.getProjects().contains(savedProject)) {
                user.getProjects().add(savedProject);
                userRepository.save(user);
                log.debug("Kullanıcıya proje ilişkisi eklendi");
            }

            responseDTO.setProjectId(savedProject.getId());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_EKLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (ValidationServiceException ex) {
            log.error("Validasyon hatası: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (BusinessServiceException ex) {
            log.error("İş kuralı hatası: {} - {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (Exception ex) {
            log.error("Beklenmeyen hata oluştu: {}", ex.getMessage(), ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("SISTEM_HATASI", "Sistem hatası oluştu: " + ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    @Transactional
    public SirketProjeGuncelleResponseDTO projeGuncelle(Long projectId, SirketProjeGuncelleRequestDTO requestDTO) {
        SirketProjeGuncelleResponseDTO responseDTO = new SirketProjeGuncelleResponseDTO();

        try {
            log.info("Proje güncelleme işlemi başlatıldı: projectId={}", projectId);

            User user = getAuthenticatedUser();

            CompanyProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI, "Proje bulunamadı"));

            // Yetki kontrolü
            if (!project.getUser().getId().equals(user.getId())) {
                throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Bu projeyi güncelleme yetkiniz yok");
            }

            // Dosya formatı validasyonları
            if (requestDTO.getNewImages() != null && !requestDTO.getNewImages().isEmpty()) {
                validateFiles(requestDTO.getNewImages(), ALLOWED_IMAGE_TYPES, "Görsel");
            }
            if (requestDTO.getNewFloorPlans() != null && !requestDTO.getNewFloorPlans().isEmpty()) {
                validateFiles(requestDTO.getNewFloorPlans(), ALLOWED_FLOORPLAN_TYPES, "Kat planı");
            }

            // Proje bilgilerini güncelle
            updateProjectFields(project, requestDTO);

            // Yeni görseller ekle
            if (requestDTO.getNewImages() != null && !requestDTO.getNewImages().isEmpty()) {
                if (project.getImages() == null) {
                    project.setImages(new ArrayList<>());
                }
                for (MultipartFile file : requestDTO.getNewImages()) {
                    if (!file.isEmpty()) {
                        try {
                            ProjectImage image = createProjectImage(file, project);
                            project.getImages().add(image);
                            log.debug("Yeni görsel eklendi: {}", file.getOriginalFilename());
                        } catch (IOException e) {
                            log.error("Görsel yüklenirken hata: {}", file.getOriginalFilename(), e);
                            throw new BusinessServiceException(MSG_DOSYA_YUKLEME_HATASI,
                                    "Görsel yüklenirken hata oluştu: " + file.getOriginalFilename());
                        }
                    }
                }
            }

            // Yeni kat planları ekle
            if (requestDTO.getNewFloorPlans() != null && !requestDTO.getNewFloorPlans().isEmpty()) {
                if (project.getFloorPlans() == null) {
                    project.setFloorPlans(new ArrayList<>());
                }
                for (MultipartFile file : requestDTO.getNewFloorPlans()) {
                    if (!file.isEmpty()) {
                        try {
                            ProjectFile floorPlan = createProjectFile(file, project);
                            project.getFloorPlans().add(floorPlan);
                            log.debug("Yeni kat planı eklendi: {}", file.getOriginalFilename());
                        } catch (IOException e) {
                            log.error("Kat planı yüklenirken hata: {}", file.getOriginalFilename(), e);
                            throw new BusinessServiceException(MSG_DOSYA_YUKLEME_HATASI,
                                    "Kat planı yüklenirken hata oluştu: " + file.getOriginalFilename());
                        }
                    }
                }
            }

            projectRepository.save(project);
            log.info("Proje güncellendi: ID={}", project.getId());

            responseDTO.setProjectId(project.getId());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode(MSG_PROJE_GUNCELLEME_BASARILI, AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException | ValidationServiceException ex) {
            log.error("Proje güncelleme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        } catch (Exception ex) {
            log.error("Beklenmeyen hata oluştu", ex);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("SISTEM_HATASI", "Sistem hatası oluştu: " + ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    @Transactional
    public SirketProjeSilResponseDTO projeSil(Long projectId) {
        SirketProjeSilResponseDTO responseDTO = new SirketProjeSilResponseDTO();

        try {
            log.info("Proje silme işlemi başlatıldı: projectId={}", projectId);

            User user = getAuthenticatedUser();

            CompanyProject project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new BusinessServiceException(MSG_PROJE_BULUNAMADI, "Proje bulunamadı"));

            if (!project.getUser().getId().equals(user.getId())) {
                throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Bu projeyi silme yetkiniz yok");
            }

            projectRepository.delete(project);
            log.info("Proje silindi: ID={}", projectId);

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

    public SirketProjelerListeleResponseDTO projeListele() {
        SirketProjelerListeleResponseDTO responseDTO = new SirketProjelerListeleResponseDTO();

        try {
            User user = getAuthenticatedUser();

            List<CompanyProject> projects = projectRepository.findByUserId(user.getId());
            log.info("Kullanıcının projeleri listelendi: {} adet", projects.size());

            List<ProjeDetayDTO> projeDetaylar = projects.stream()
                    .map(this::convertToProjeDetayDTO)
                    .collect(Collectors.toList());

            responseDTO.setProjeler(projeDetaylar);
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithCode("LISTE_BASARILI", AppMessageType.SUCCESS)
            ));

        } catch (BusinessServiceException ex) {
            log.error("Proje listeleme hatası: {}", ex.getMessage());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create(ex.getClass().getSimpleName(), ex.getMessage(), AppMessageType.ERROR)
            ));
        }

        return responseDTO;
    }

    // ===== HELPER METHODS =====

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessServiceException(MSG_YETKISIZ_ERISIM, "Kullanıcı doğrulanmamış");
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessServiceException(MSG_KULLANICI_BULUNAMADI, "Kullanıcı bulunamadı"));
    }

    private void validateProjectRequest(SirketProjeAyarlaRequestDTO requestDTO) {
        if (requestDTO.getProjectName() == null || requestDTO.getProjectName().isBlank()) {
            throw new ValidationServiceException("PROJE_ADI_BOS", "Proje adı boş olamaz");
        }
        if (requestDTO.getCategory() == null) {
            throw new ValidationServiceException("KATEGORI_BOS", "Kategori boş olamaz");
        }
        if (requestDTO.getStatus() == null) {
            throw new ValidationServiceException("DURUM_BOS", "Durum boş olamaz");
        }
        if (requestDTO.getLocation() == null || requestDTO.getLocation().isBlank()) {
            throw new ValidationServiceException("LOKASYON_BOS", "Lokasyon boş olamaz");
        }
    }

    private void validateFiles(List<MultipartFile> files, List<String> allowedTypes, String fileTypeLabel) {
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                log.warn("{} dosyası boş, atlanıyor", fileTypeLabel);
                continue;
            }

            String contentType = file.getContentType();
            String fileName = file.getOriginalFilename();

            log.debug("Dosya kontrol ediliyor - Ad: {}, Tip: {}, Boyut: {} bytes",
                    fileName, contentType, file.getSize());

            if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
                String allowedTypesStr = String.join(", ", allowedTypes);
                String errorMsg = String.format(
                        "%s dosyası geçersiz format. Dosya: '%s', Gönderilen tip: '%s', İzin verilen tipler: [%s]",
                        fileTypeLabel, fileName, contentType, allowedTypesStr
                );
                log.error(errorMsg);
                throw new ValidationServiceException(MSG_GECERSIZ_DOSYA_FORMATI, errorMsg);
            }

            // Dosya boyutu kontrolü
            if (file.getSize() > MAX_FILE_SIZE) {
                String errorMsg = String.format(
                        "%s dosyası çok büyük. Dosya: '%s', Boyut: %.2f MB, Maksimum: 10MB",
                        fileTypeLabel, fileName, file.getSize() / (1024.0 * 1024.0)
                );
                log.error(errorMsg);
                throw new ValidationServiceException("DOSYA_BOYUTU_FAZLA", errorMsg);
            }

            // Dosya adı kontrolü
            if (fileName == null || fileName.isBlank()) {
                String errorMsg = fileTypeLabel + " dosyasının adı geçersiz";
                log.error(errorMsg);
                throw new ValidationServiceException("GECERSIZ_DOSYA_ADI", errorMsg);
            }

            log.debug("Dosya validasyonu başarılı: {}", fileName);
        }
    }

    private void updateProjectFields(CompanyProject project, SirketProjeGuncelleRequestDTO requestDTO) {
        if (requestDTO.getProjectName() != null && !requestDTO.getProjectName().isBlank()) {
            project.setProjectName(requestDTO.getProjectName());
        }
        if (requestDTO.getCategory() != null) {
            project.setCategory(requestDTO.getCategory());
        }
        if (requestDTO.getLocation() != null) {
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
        if (requestDTO.getDescription() != null) {
            project.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getTechnicalSpecifications() != null) {
            project.setTechnicalSpecifications(requestDTO.getTechnicalSpecifications());
        }
        if (requestDTO.getFeatures() != null) {
            project.setFeatures(requestDTO.getFeatures());
        }
    }

    private ProjectImage createProjectImage(MultipartFile file, CompanyProject project) throws IOException {
        return ProjectImage.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .imageData(file.getBytes())
                .project(project)  // ✅ İlişki set ediliyor
                .build();
    }

    private ProjectFile createProjectFile(MultipartFile file, CompanyProject project) throws IOException {
        return ProjectFile.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileData(file.getBytes())
                .project(project)  // ✅ İlişki set ediliyor
                .build();
    }

    private ProjeDetayDTO convertToProjeDetayDTO(CompanyProject project) {
        return ProjeDetayDTO.builder()
                .id(project.getId())
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
                .imageCount(project.getImages() != null ? project.getImages().size() : 0)
                .floorPlanCount(project.getFloorPlans() != null ? project.getFloorPlans().size() : 0)
                .build();
    }
}