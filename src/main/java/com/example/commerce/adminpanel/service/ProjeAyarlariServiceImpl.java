package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.FloorPlanRoomDetail;
import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.enums.FileCategory;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.adminpanel.repository.ProjectFileRepository;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.service.AuthenticationService;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjeAyarlariServiceImpl implements ProjeAyarlariService {

    private static final String MSG_PROJE_EKLEME_BASARILI = "proje.ekleme.basarili";
    private static final String MSG_PROJE_GUNCELLEME_BASARILI = "proje.guncelleme.basarili";
    private static final String MSG_PROJE_SILME_BASARILI = "proje.silme.basarili";
    private static final String MSG_PROJE_BULUNAMADI = "proje.bulunamadi";
    private static final String MSG_DOSYA_BULUNAMADI = "dosya.bulunamadi";
    private static final String MSG_DOSYA_SILME_BASARILI = "dosya.silme.basarili";
    private static final String MSG_DOSYA_HATASI = "dosya.hatasi";

    // DB kolon uzunluklarıyla uyumlu (bkz. 001-baseline.xml) girdi sınırları
    private static final int MAX_PROJECT_NAME_LENGTH = 255;
    private static final int MAX_LOCATION_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    // Tek bir tenant'ın (şirketin) listeleri/dosyalarla paylaşılan DB'yi
    // şişirmesini önlemek için makul üst sınırlar
    private static final int MAX_LIST_SIZE = 50;
    private static final int MAX_SPEC_LENGTH = 2000;
    private static final int MAX_FEATURE_LENGTH = 500;

    private static final int MAX_IMAGE_COUNT_PER_PROJECT = 20;
    private static final int MAX_FLOOR_PLAN_COUNT_PER_PROJECT = 10;
    private static final long MAX_IMAGE_SIZE_BYTES = 10L * 1024 * 1024;
    private static final long MAX_FLOOR_PLAN_SIZE_BYTES = 20L * 1024 * 1024;

    private static final int MAX_FLOOR_PLAN_TITLE_LENGTH = 255;
    private static final int MAX_ROOM_DETAILS_PER_FLOOR_PLAN = 30;
    private static final int MAX_ROOM_NAME_LENGTH = 100;
    private static final int MAX_ROOM_DETAIL_VALUE_LENGTH = 500;

    private static final int MAX_PAGE_SIZE = 100;

    private final CompanyProjectRepository projectRepository;
    private final ProjectFileRepository fileRepository;
    private final AuthenticationService authenticationService;

    @Override
    @Transactional
    public SirketProjeAyarlaResponseDTO projeEkle(
            SirketProjeAyarlaRequestDTO requestDTO,
            List<MultipartFile> images,
            List<MultipartFile> floorPlans) {

        validateProjeRequest(requestDTO);

        log.info("Proje ekleme işlemi başlatıldı: {}", requestDTO.getProjectName());

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

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
                .company(company)
                .createdByUser(user)
                .updatedByUser(user)
                .build();

        validateFileUpload(project, images, FileCategory.IMAGE, MAX_IMAGE_COUNT_PER_PROJECT, MAX_IMAGE_SIZE_BYTES, "Resim");
        validateFileUpload(project, floorPlans, FileCategory.FLOOR_PLAN, MAX_FLOOR_PLAN_COUNT_PER_PROJECT, MAX_FLOOR_PLAN_SIZE_BYTES, "Kat planı");

        resimleriEkle(project, images);
        katPlanlariniEkle(project, floorPlans, requestDTO.getFloorPlanDetails());

        CompanyProject savedProject = projectRepository.save(project);
        log.info("Proje kaydedildi: ID={}, Ad={}, Dosya Sayısı={}",
                savedProject.getId(), savedProject.getProjectName(), savedProject.getFiles().size());

        SirketProjeAyarlaResponseDTO responseDTO = new SirketProjeAyarlaResponseDTO();
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_PROJE_EKLEME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public SirketProjelerListeleResponseDTO projeleriListele(ProjeleriListeleRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();

        Pageable pageable = request.toPageable(MAX_PAGE_SIZE);
        Page<CompanyProject> projects = projectRepository.findByCompanyAndDeletedFalse(company, pageable);
        log.info("Kullanıcının projeleri listelendi: {} adet (sayfa {}/{})",
                projects.getNumberOfElements(), projects.getNumber(), projects.getTotalPages());

        SirketProjelerListeleResponseDTO responseDTO = new SirketProjelerListeleResponseDTO();
        responseDTO.loadFrom(projects, this::convertToProjeListDTO);

        if (projects.isEmpty()) {
            responseDTO.setMessages(List.of(
                    AppMessageUtil.createWithText("Henüz proje eklenmemiş", AppMessageType.INFO)
            ));
        }

        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjeDetayGetirResponseDTO projeDetayGetir(Long projeId) {
        Company company = authenticationService.getAuthenticatedUserCompany();

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROJE_BULUNAMADI,
                        "Proje bulunamadı veya erişim yetkiniz yok"));

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
                        .filter(f -> !f.isDeleted())
                        .map(this::convertToFileDetailDTO)
                        .toList())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();

        ProjeDetayGetirResponseDTO responseDTO = new ProjeDetayGetirResponseDTO();
        responseDTO.setData(detay);
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public DosyaIndirResponseDTO dosyaIndir(Long dosyaId) {
        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        ProjectFile file = fileRepository.findByIdAndDeletedFalseAndProject_CompanyAndProject_DeletedFalse(dosyaId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_DOSYA_BULUNAMADI,
                        "Dosya bulunamadı veya erişim yetkiniz yok"));

        log.info("Dosya indiriliyor: User={}, Dosya ID={}, Ad={}",
                user.getEmail(), dosyaId, file.getFileName());

        DosyaIndirResponseDTO responseDTO = new DosyaIndirResponseDTO();
        responseDTO.setData(convertToFileDetailDTO(file));
        return responseDTO;
    }

    @Override
    @Transactional
    public SirketProjeSilResponseDTO projeSoftDelete(Long projeId) {
        log.info("Proje silme işlemi başlatıldı: projeId={}", projeId);

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROJE_BULUNAMADI,
                        "Proje bulunamadı veya erişim yetkiniz yok"));

        project.setDeleted(true);
        project.setUpdatedByUser(user);
        projectRepository.save(project);

        log.info("Proje silindi (soft delete): ID={}, Kullanıcı={}", projeId, user.getEmail());

        SirketProjeSilResponseDTO responseDTO = new SirketProjeSilResponseDTO();
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_PROJE_SILME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    @Override
    @Transactional
    public DosyaSilResponseDTO dosyaSil(Long projeId, Long dosyaId) {
        log.info("Dosya silme işlemi başlatıldı: projeId={}, dosyaId={}", projeId, dosyaId);

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROJE_BULUNAMADI,
                        "Proje bulunamadı veya erişim yetkiniz yok"));

        ProjectFile fileToRemove = project.getFiles().stream()
                .filter(f -> f.getId().equals(dosyaId) && !f.isDeleted())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(MSG_DOSYA_BULUNAMADI, "Dosya bulunamadı"));

        fileToRemove.setDeleted(true);
        fileRepository.save(fileToRemove);

        log.info("Dosya silindi (soft delete): Proje ID={}, Dosya ID={}, Dosya Adı={}, Kullanıcı={}",
                projeId, dosyaId, fileToRemove.getFileName(), user.getEmail());

        DosyaSilResponseDTO responseDTO = new DosyaSilResponseDTO();
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_DOSYA_SILME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    @Override
    @Transactional
    public SirketProjeGuncelleResponseDTO projeGuncelle(
            Long projectId,
            SirketProjeGuncelleRequestDTO requestDTO,
            List<MultipartFile> newImages,
            List<MultipartFile> newFloorPlans) {

        log.info("Proje güncelleme işlemi başlatıldı: projectId={}", projectId);

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projectId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROJE_BULUNAMADI,
                        "Proje bulunamadı veya erişim yetkiniz yok"));

        validateProjeGuncelleRequest(requestDTO);
        updateProjectFields(project, requestDTO);
        validateDateOrder(project.getStartDate(), project.getEndDate());
        project.setUpdatedByUser(user);

        validateFileUpload(project, newImages, FileCategory.IMAGE, MAX_IMAGE_COUNT_PER_PROJECT, MAX_IMAGE_SIZE_BYTES, "Resim");
        validateFileUpload(project, newFloorPlans, FileCategory.FLOOR_PLAN, MAX_FLOOR_PLAN_COUNT_PER_PROJECT, MAX_FLOOR_PLAN_SIZE_BYTES, "Kat planı");

        resimleriEkle(project, newImages);
        katPlanlariniEkle(project, newFloorPlans, requestDTO.getNewFloorPlanDetails());

        CompanyProject updatedProject = projectRepository.save(project);
        log.info("Proje güncellendi: ID={}, Ad={}, Toplam Dosya={}",
                updatedProject.getId(), updatedProject.getProjectName(), updatedProject.getFiles().size());

        SirketProjeGuncelleResponseDTO responseDTO = new SirketProjeGuncelleResponseDTO();
        responseDTO.setProjectId(updatedProject.getId());
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_PROJE_GUNCELLEME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    private void validateProjeRequest(SirketProjeAyarlaRequestDTO requestDTO) {
        if (requestDTO.getProjectName() == null || requestDTO.getProjectName().isBlank()) {
            throw new ValidationServiceException("proje.ad.zorunlu", "Proje adı zorunludur");
        }
        if (requestDTO.getCategory() == null) {
            throw new ValidationServiceException("proje.kategori.zorunlu", "Proje kategorisi zorunludur");
        }
        if (requestDTO.getLocation() == null || requestDTO.getLocation().isBlank()) {
            throw new ValidationServiceException("proje.konum.zorunlu", "Proje konumu zorunludur");
        }
        if (requestDTO.getStatus() == null) {
            throw new ValidationServiceException("proje.durum.zorunlu", "Proje durumu zorunludur");
        }

        validateTextLength(requestDTO.getProjectName(), MAX_PROJECT_NAME_LENGTH, "Proje adı");
        validateTextLength(requestDTO.getLocation(), MAX_LOCATION_LENGTH, "Konum");
        validateTextLength(requestDTO.getDescription(), MAX_DESCRIPTION_LENGTH, "Açıklama");
        validateStringList(requestDTO.getTechnicalSpecifications(), MAX_SPEC_LENGTH, "Teknik özellik");
        validateStringList(requestDTO.getFeatures(), MAX_FEATURE_LENGTH, "Özellik");
        validateNumericSanity(requestDTO.getTotalArea(), requestDTO.getDurationMonths());
        validateDateOrder(requestDTO.getStartDate(), requestDTO.getEndDate());
    }

    private void validateProjeGuncelleRequest(SirketProjeGuncelleRequestDTO requestDTO) {
        validateTextLength(requestDTO.getProjectName(), MAX_PROJECT_NAME_LENGTH, "Proje adı");
        validateTextLength(requestDTO.getLocation(), MAX_LOCATION_LENGTH, "Konum");
        validateTextLength(requestDTO.getDescription(), MAX_DESCRIPTION_LENGTH, "Açıklama");
        validateStringList(requestDTO.getTechnicalSpecifications(), MAX_SPEC_LENGTH, "Teknik özellik");
        validateStringList(requestDTO.getFeatures(), MAX_FEATURE_LENGTH, "Özellik");
        validateNumericSanity(requestDTO.getTotalArea(), requestDTO.getDurationMonths());
    }

    private void validateTextLength(String value, int maxLength, String fieldLabel) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationServiceException("proje.metin.uzunluk",
                    fieldLabel + " en fazla " + maxLength + " karakter olabilir");
        }
    }

    private void validateStringList(List<String> values, int maxItemLength, String fieldLabel) {
        if (values == null) return;
        if (values.size() > MAX_LIST_SIZE) {
            throw new ValidationServiceException("proje.liste.limit",
                    fieldLabel + " listesi en fazla " + MAX_LIST_SIZE + " öğe içerebilir");
        }
        for (String value : values) {
            if (value != null && value.length() > maxItemLength) {
                throw new ValidationServiceException("proje.liste.uzunluk",
                        fieldLabel + " öğesi en fazla " + maxItemLength + " karakter olabilir");
            }
        }
    }

    private void validateNumericSanity(Double totalArea, Integer durationMonths) {
        if (totalArea != null && totalArea < 0) {
            throw new ValidationServiceException("proje.alan.gecersiz", "Toplam alan negatif olamaz");
        }
        if (durationMonths != null && (durationMonths < 0 || durationMonths > 600)) {
            throw new ValidationServiceException("proje.sure.gecersiz", "Süre (ay) 0-600 aralığında olmalıdır");
        }
    }

    private void validateDateOrder(LocalDate startDate, LocalDate endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new ValidationServiceException("proje.tarih.gecersiz", "Bitiş tarihi başlangıç tarihinden önce olamaz");
        }
    }

    private void validateFileUpload(CompanyProject project, List<MultipartFile> files, FileCategory category,
                                     int maxCountPerProject, long maxSizeBytes, String label) {
        if (files == null || files.isEmpty()) return;

        long existingCount = project.getFiles().stream()
                .filter(f -> f.getFileCategory() == category && !f.isDeleted())
                .count();
        if (existingCount + files.size() > maxCountPerProject) {
            throw new ValidationServiceException("proje.dosya.limit",
                    label + " sayısı proje başına en fazla " + maxCountPerProject + " olabilir");
        }
        for (MultipartFile file : files) {
            if (file.getSize() > maxSizeBytes) {
                throw new ValidationServiceException("proje.dosya.boyut",
                        (file.getOriginalFilename() != null ? file.getOriginalFilename() : label) +
                                " dosyası izin verilen " + (maxSizeBytes / (1024 * 1024)) + "MB sınırını aşıyor");
            }
        }
    }

    private void resimleriEkle(CompanyProject project, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) return;
        for (MultipartFile file : images) {
            project.getFiles().add(buildProjectFile(project, file, FileCategory.IMAGE, null, null));
        }
    }

    private void katPlanlariniEkle(CompanyProject project, List<MultipartFile> floorPlans, List<FloorPlanInputDTO> floorPlanDetails) {
        if (floorPlans == null || floorPlans.isEmpty()) return;

        for (int i = 0; i < floorPlans.size(); i++) {
            FloorPlanInputDTO input = (floorPlanDetails != null && i < floorPlanDetails.size())
                    ? floorPlanDetails.get(i) : null;
            String title = input != null ? input.getTitle() : null;
            validateTextLength(title, MAX_FLOOR_PLAN_TITLE_LENGTH, "Kat planı başlığı");
            List<FloorPlanRoomDetail> roomDetails = validateAndMapRoomDetails(input != null ? input.getRoomDetails() : null);

            project.getFiles().add(buildProjectFile(project, floorPlans.get(i), FileCategory.FLOOR_PLAN, title, roomDetails));
        }
    }

    private ProjectFile buildProjectFile(CompanyProject project, MultipartFile file, FileCategory category,
                                          String title, List<FloorPlanRoomDetail> roomDetails) {
        try {
            return ProjectFile.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileData(file.getBytes())
                    .fileCategory(category)
                    .uploadDate(LocalDateTime.now())
                    .project(project)
                    .title(title)
                    .roomDetails(roomDetails != null ? roomDetails : new ArrayList<>())
                    .build();
        } catch (IOException ex) {
            log.error("Dosya işleme hatası: {}", ex.getMessage(), ex);
            throw new BusinessServiceException(MSG_DOSYA_HATASI, "Dosya yüklenirken hata oluştu: " + file.getOriginalFilename());
        }
    }

    private List<FloorPlanRoomDetail> validateAndMapRoomDetails(List<FloorPlanRoomDetailDTO> input) {
        if (input == null) return new ArrayList<>();
        if (input.size() > MAX_ROOM_DETAILS_PER_FLOOR_PLAN) {
            throw new ValidationServiceException("proje.katplani.oda.limit",
                    "Bir kat planına en fazla " + MAX_ROOM_DETAILS_PER_FLOOR_PLAN + " oda/alan bilgisi eklenebilir");
        }

        List<FloorPlanRoomDetail> result = new ArrayList<>();
        for (FloorPlanRoomDetailDTO dto : input) {
            if (dto == null) continue;
            validateTextLength(dto.getRoomName(), MAX_ROOM_NAME_LENGTH, "Oda adı");
            validateTextLength(dto.getValue(), MAX_ROOM_DETAIL_VALUE_LENGTH, "Oda bilgisi");
            result.add(FloorPlanRoomDetail.builder()
                    .roomName(dto.getRoomName())
                    .value(dto.getValue())
                    .build());
        }
        return result;
    }

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
                .files(toFileMetaDTOs(fileRepository.findMetaByProjectAndDeletedFalse(project)))
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .build();
    }

    /** Dosya içeriğini (bytea) hiç DB'den çekmeden yalnızca metadata döndürür. */
    private List<ProjectFileMetaDTO> toFileMetaDTOs(List<ProjectFileRepository.ProjectFileMetaView> views) {
        if (views.isEmpty()) return List.of();

        List<Long> fileIds = views.stream().map(ProjectFileRepository.ProjectFileMetaView::getId).toList();
        Map<Long, List<FloorPlanRoomDetailDTO>> roomDetailsByFileId = fileRepository.findRoomDetailRows(fileIds).stream()
                .collect(Collectors.groupingBy(ProjectFileRepository.RoomDetailRow::getFileId,
                        Collectors.mapping(rd -> FloorPlanRoomDetailDTO.builder()
                                        .roomName(rd.getRoomName())
                                        .value(rd.getValue())
                                        .build(),
                                Collectors.toList())));

        return views.stream()
                .map(v -> ProjectFileMetaDTO.builder()
                        .id(v.getId())
                        .fileName(v.getFileName())
                        .fileType(v.getFileType())
                        .fileSize(v.getFileSize())
                        .fileCategory(v.getFileCategory())
                        .title(v.getTitle())
                        .roomDetails(roomDetailsByFileId.getOrDefault(v.getId(), List.of()))
                        .uploadDate(v.getUploadDate())
                        .build())
                .toList();
    }

    private ProjectFileDetailDTO convertToFileDetailDTO(ProjectFile file) {
        return ProjectFileDetailDTO.builder()
                .id(file.getId())
                .fileName(file.getFileName())
                .fileType(file.getFileType())
                .fileSize(file.getFileSize())
                .fileCategory(file.getFileCategory())
                .fileData(Base64.getEncoder().encodeToString(file.getFileData()))
                .title(file.getTitle())
                .roomDetails(mapRoomDetailDTOs(file.getRoomDetails()))
                .uploadDate(file.getUploadDate())
                .build();
    }

    private List<FloorPlanRoomDetailDTO> mapRoomDetailDTOs(List<FloorPlanRoomDetail> roomDetails) {
        if (roomDetails == null) return List.of();
        return roomDetails.stream()
                .map(rd -> FloorPlanRoomDetailDTO.builder().roomName(rd.getRoomName()).value(rd.getValue()).build())
                .toList();
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
