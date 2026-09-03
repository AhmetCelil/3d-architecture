package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.FloorPlanRoomDetail;
import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.entity.UnitType;
import com.example.commerce.adminpanel.enums.FileCategory;
import com.example.commerce.adminpanel.repository.CompanyProjectRepository;
import com.example.commerce.adminpanel.repository.ProjectFileRepository;
import com.example.commerce.adminpanel.repository.UnitTypeRepository;
import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.service.AuthenticationService;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.common.cache.FileByteCache;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UniteTipiServiceImpl implements UniteTipiService {

    private static final String MSG_UNITE_EKLEME_BASARILI = "unite.ekleme.basarili";
    private static final String MSG_UNITE_GUNCELLEME_BASARILI = "unite.guncelleme.basarili";
    private static final String MSG_UNITE_SILME_BASARILI = "unite.silme.basarili";
    private static final String MSG_UNITE_BULUNAMADI = "unite.bulunamadi";
    private static final String MSG_PROJE_BULUNAMADI = "proje.bulunamadi";
    private static final String MSG_DOSYA_BULUNAMADI = "dosya.bulunamadi";
    private static final String MSG_DOSYA_SILME_BASARILI = "dosya.silme.basarili";
    private static final String MSG_DOSYA_HATASI = "dosya.hatasi";

    private static final int MAX_LABEL_LENGTH = 255;
    private static final int MAX_BLOCK_LABEL_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 2000;

    private static final int MAX_ROOM_DETAILS = 30;
    private static final int MAX_ROOM_NAME_LENGTH = 100;
    private static final int MAX_ROOM_DETAIL_VALUE_LENGTH = 500;

    private static final int MAX_FLOOR_PLAN_TITLE_LENGTH = 255;
    private static final int MAX_FLOOR_PLAN_COUNT_PER_UNIT_TYPE = 10;
    private static final long MAX_FLOOR_PLAN_SIZE_BYTES = 20L * 1024 * 1024;

    private final UnitTypeRepository unitTypeRepository;
    private final CompanyProjectRepository projectRepository;
    private final ProjectFileRepository fileRepository;
    private final AuthenticationService authenticationService;
    private final FileByteCache fileByteCache;

    @Override
    @Transactional
    public UniteTipiEkleResponseDTO uniteTipiEkle(Long projeId, UniteTipiInputDTO requestDTO, List<MultipartFile> floorPlans) {
        log.info("Ünite tipi ekleme işlemi başlatıldı: projeId={}", projeId);

        validateUniteTipiRequest(requestDTO, true);

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROJE_BULUNAMADI,
                        "Proje bulunamadı veya erişim yetkiniz yok"));

        UnitType unitType = UnitType.builder()
                .project(project)
                .blockLabel(requestDTO.getBlockLabel())
                .label(requestDTO.getLabel())
                .area(requestDTO.getArea())
                .roomCount(requestDTO.getRoomCount())
                .description(requestDTO.getDescription())
                .roomDetails(validateAndMapRoomDetails(requestDTO.getRoomDetails()))
                .createdByUser(user)
                .updatedByUser(user)
                .build();

        validateFloorPlanUpload(unitType, floorPlans);
        katPlanlariniEkle(unitType, floorPlans, requestDTO.getFloorPlanTitles());

        UnitType saved = unitTypeRepository.save(unitType);
        log.info("Ünite tipi kaydedildi: ID={}, Proje ID={}, Etiket={}", saved.getId(), projeId, saved.getLabel());

        UniteTipiEkleResponseDTO responseDTO = new UniteTipiEkleResponseDTO();
        responseDTO.setUnitTypeId(saved.getId());
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_UNITE_EKLEME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public UniteTipleriListeleResponseDTO uniteTipleriListele(Long projeId) {
        Company company = authenticationService.getAuthenticatedUserCompany();

        CompanyProject project = projectRepository.findByIdAndCompanyAndDeletedFalse(projeId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_PROJE_BULUNAMADI,
                        "Proje bulunamadı veya erişim yetkiniz yok"));

        List<UnitType> unitTypes = unitTypeRepository.findByProjectAndDeletedFalse(project);
        log.info("Proje ünite tipleri listelendi: projeId={}, adet={}", projeId, unitTypes.size());

        UniteTipleriListeleResponseDTO responseDTO = new UniteTipleriListeleResponseDTO();
        responseDTO.setData(unitTypes.stream().map(this::convertToUniteTipiDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public UniteTipiDetayGetirResponseDTO uniteTipiDetayGetir(Long uniteTipiId) {
        Company company = authenticationService.getAuthenticatedUserCompany();

        UnitType unitType = unitTypeRepository.findByIdAndProject_CompanyAndDeletedFalse(uniteTipiId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_UNITE_BULUNAMADI,
                        "Ünite tipi bulunamadı veya erişim yetkiniz yok"));

        UniteTipiDetayGetirResponseDTO responseDTO = new UniteTipiDetayGetirResponseDTO();
        responseDTO.setData(convertToUniteTipiDTO(unitType));
        return responseDTO;
    }

    @Override
    @Transactional
    public UniteTipiGuncelleResponseDTO uniteTipiGuncelle(Long uniteTipiId, UniteTipiInputDTO requestDTO, List<MultipartFile> newFloorPlans) {
        log.info("Ünite tipi güncelleme işlemi başlatıldı: uniteTipiId={}", uniteTipiId);

        validateUniteTipiRequest(requestDTO, false);

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        UnitType unitType = unitTypeRepository.findByIdAndProject_CompanyAndDeletedFalse(uniteTipiId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_UNITE_BULUNAMADI,
                        "Ünite tipi bulunamadı veya erişim yetkiniz yok"));

        updateUniteTipiFields(unitType, requestDTO);
        unitType.setUpdatedByUser(user);

        validateFloorPlanUpload(unitType, newFloorPlans);
        katPlanlariniEkle(unitType, newFloorPlans, requestDTO.getFloorPlanTitles());

        UnitType updated = unitTypeRepository.save(unitType);
        log.info("Ünite tipi güncellendi: ID={}", updated.getId());

        UniteTipiGuncelleResponseDTO responseDTO = new UniteTipiGuncelleResponseDTO();
        responseDTO.setUnitTypeId(updated.getId());
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_UNITE_GUNCELLEME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    @Override
    @Transactional
    public UniteTipiSilResponseDTO uniteTipiSoftDelete(Long uniteTipiId) {
        log.info("Ünite tipi silme işlemi başlatıldı: uniteTipiId={}", uniteTipiId);

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        UnitType unitType = unitTypeRepository.findByIdAndProject_CompanyAndDeletedFalse(uniteTipiId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_UNITE_BULUNAMADI,
                        "Ünite tipi bulunamadı veya erişim yetkiniz yok"));

        unitType.setDeleted(true);
        unitType.setUpdatedByUser(user);
        unitTypeRepository.save(unitType);

        log.info("Ünite tipi silindi (soft delete): ID={}, Kullanıcı={}", uniteTipiId, user.getEmail());

        UniteTipiSilResponseDTO responseDTO = new UniteTipiSilResponseDTO();
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_UNITE_SILME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    @Override
    @Transactional
    public DosyaSilResponseDTO uniteTipiDosyaSil(Long uniteTipiId, Long dosyaId) {
        log.info("Ünite tipi dosya silme işlemi başlatıldı: uniteTipiId={}, dosyaId={}", uniteTipiId, dosyaId);

        User user = authenticationService.getAuthenticatedUser();
        Company company = authenticationService.getAuthenticatedUserCompany();

        UnitType unitType = unitTypeRepository.findByIdAndProject_CompanyAndDeletedFalse(uniteTipiId, company)
                .orElseThrow(() -> new ResourceNotFoundException(MSG_UNITE_BULUNAMADI,
                        "Ünite tipi bulunamadı veya erişim yetkiniz yok"));

        ProjectFile fileToRemove = unitType.getFiles().stream()
                .filter(f -> f.getId().equals(dosyaId) && !f.isDeleted())
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(MSG_DOSYA_BULUNAMADI, "Dosya bulunamadı"));

        fileToRemove.setDeleted(true);
        fileRepository.save(fileToRemove);
        fileByteCache.evict("projectFile:" + dosyaId);

        log.info("Ünite tipi dosyası silindi (soft delete): Ünite Tipi ID={}, Dosya ID={}, Kullanıcı={}",
                uniteTipiId, dosyaId, user.getEmail());

        DosyaSilResponseDTO responseDTO = new DosyaSilResponseDTO();
        responseDTO.setMessages(List.of(
                AppMessageUtil.createWithCode(MSG_DOSYA_SILME_BASARILI, AppMessageType.SUCCESS)
        ));
        return responseDTO;
    }

    private void validateUniteTipiRequest(UniteTipiInputDTO requestDTO, boolean isCreate) {
        if (isCreate && (requestDTO.getLabel() == null || requestDTO.getLabel().isBlank())) {
            throw new ValidationServiceException("unite.etiket.zorunlu", "Ünite tipi etiketi zorunludur");
        }
        validateTextLength(requestDTO.getLabel(), MAX_LABEL_LENGTH, "Ünite tipi etiketi");
        validateTextLength(requestDTO.getBlockLabel(), MAX_BLOCK_LABEL_LENGTH, "Blok etiketi");
        validateTextLength(requestDTO.getDescription(), MAX_DESCRIPTION_LENGTH, "Açıklama");

        if (requestDTO.getArea() != null && requestDTO.getArea() < 0) {
            throw new ValidationServiceException("unite.alan.gecersiz", "Alan negatif olamaz");
        }
        if (requestDTO.getRoomCount() != null && (requestDTO.getRoomCount() < 0 || requestDTO.getRoomCount() > 100)) {
            throw new ValidationServiceException("unite.oda.gecersiz", "Oda sayısı 0-100 aralığında olmalıdır");
        }
    }

    private void validateTextLength(String value, int maxLength, String fieldLabel) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationServiceException("unite.metin.uzunluk",
                    fieldLabel + " en fazla " + maxLength + " karakter olabilir");
        }
    }

    private List<FloorPlanRoomDetail> validateAndMapRoomDetails(List<FloorPlanRoomDetailDTO> input) {
        if (input == null) return new ArrayList<>();
        if (input.size() > MAX_ROOM_DETAILS) {
            throw new ValidationServiceException("unite.oda.limit",
                    "Bir ünite tipine en fazla " + MAX_ROOM_DETAILS + " oda/alan bilgisi eklenebilir");
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

    private void validateFloorPlanUpload(UnitType unitType, List<MultipartFile> floorPlans) {
        if (floorPlans == null || floorPlans.isEmpty()) return;

        long existingCount = unitType.getFiles().stream().filter(f -> !f.isDeleted()).count();
        if (existingCount + floorPlans.size() > MAX_FLOOR_PLAN_COUNT_PER_UNIT_TYPE) {
            throw new ValidationServiceException("unite.dosya.limit",
                    "Kat planı sayısı ünite tipi başına en fazla " + MAX_FLOOR_PLAN_COUNT_PER_UNIT_TYPE + " olabilir");
        }
        for (MultipartFile file : floorPlans) {
            if (file.getSize() > MAX_FLOOR_PLAN_SIZE_BYTES) {
                throw new ValidationServiceException("unite.dosya.boyut",
                        (file.getOriginalFilename() != null ? file.getOriginalFilename() : "Kat planı") +
                                " dosyası izin verilen " + (MAX_FLOOR_PLAN_SIZE_BYTES / (1024 * 1024)) + "MB sınırını aşıyor");
            }
        }
    }

    private void katPlanlariniEkle(UnitType unitType, List<MultipartFile> floorPlans, List<String> floorPlanTitles) {
        if (floorPlans == null || floorPlans.isEmpty()) return;

        for (int i = 0; i < floorPlans.size(); i++) {
            String title = (floorPlanTitles != null && i < floorPlanTitles.size()) ? floorPlanTitles.get(i) : null;
            validateTextLength(title, MAX_FLOOR_PLAN_TITLE_LENGTH, "Kat planı başlığı");
            unitType.getFiles().add(buildProjectFile(unitType, floorPlans.get(i), title));
        }
    }

    private ProjectFile buildProjectFile(UnitType unitType, MultipartFile file, String title) {
        try {
            return ProjectFile.builder()
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileData(file.getBytes())
                    .fileCategory(FileCategory.FLOOR_PLAN)
                    .uploadDate(LocalDateTime.now())
                    .project(unitType.getProject())
                    .unitType(unitType)
                    .title(title)
                    .roomDetails(new ArrayList<>())
                    .build();
        } catch (IOException ex) {
            log.error("Dosya işleme hatası: {}", ex.getMessage(), ex);
            throw new BusinessServiceException(MSG_DOSYA_HATASI, "Dosya yüklenirken hata oluştu: " + file.getOriginalFilename());
        }
    }

    private void updateUniteTipiFields(UnitType unitType, UniteTipiInputDTO requestDTO) {
        if (requestDTO.getBlockLabel() != null) {
            unitType.setBlockLabel(requestDTO.getBlockLabel());
        }
        if (requestDTO.getLabel() != null && !requestDTO.getLabel().isBlank()) {
            unitType.setLabel(requestDTO.getLabel());
        }
        if (requestDTO.getArea() != null) {
            unitType.setArea(requestDTO.getArea());
        }
        if (requestDTO.getRoomCount() != null) {
            unitType.setRoomCount(requestDTO.getRoomCount());
        }
        if (requestDTO.getDescription() != null) {
            unitType.setDescription(requestDTO.getDescription());
        }
        if (requestDTO.getRoomDetails() != null) {
            unitType.setRoomDetails(validateAndMapRoomDetails(requestDTO.getRoomDetails()));
        }
    }

    private UniteTipiDTO convertToUniteTipiDTO(UnitType unitType) {
        return UniteTipiDTO.builder()
                .id(unitType.getId())
                .blockLabel(unitType.getBlockLabel())
                .label(unitType.getLabel())
                .area(unitType.getArea())
                .roomCount(unitType.getRoomCount())
                .description(unitType.getDescription())
                .roomDetails(mapRoomDetailDTOs(unitType.getRoomDetails()))
                .files(toFileMetaDTOs(fileRepository.findMetaByUnitTypeAndDeletedFalse(unitType)))
                .createdAt(unitType.getCreatedAt())
                .updatedAt(unitType.getUpdatedAt())
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

    private List<FloorPlanRoomDetailDTO> mapRoomDetailDTOs(List<FloorPlanRoomDetail> roomDetails) {
        if (roomDetails == null) return List.of();
        return roomDetails.stream()
                .map(rd -> FloorPlanRoomDetailDTO.builder().roomName(rd.getRoomName()).value(rd.getValue()).build())
                .toList();
    }
}
