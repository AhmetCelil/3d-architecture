package com.example.commerce.superadminpanel.service;

import com.example.commerce.adminpanel.entity.CompanyDocument;
import com.example.commerce.adminpanel.repository.CompanyDocumentRepository;
import com.example.commerce.auth.entity.User;
import com.example.commerce.common.cache.FileByteCache;
import com.example.commerce.auth.repository.UserRepository;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.rent.entity.RentSettings;
import com.example.commerce.rent.repository.RentSettingsRepository;
import com.example.commerce.superadminpanel.dto.*;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.tenant.repository.CompanyMembershipRepository;
import com.example.commerce.tenant.repository.CompanyRepository;
import com.example.commerce.util.AppMessageUtil;
import com.example.commerce.util.FileResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperAdminService {

    private static final int MAX_PAGE_SIZE = 100;
    private static final long MAX_DOCUMENT_SIZE_BYTES = 20L * 1024 * 1024;

    private final CompanyRepository companyRepository;
    private final CompanyMembershipRepository companyMembershipRepository;
    private final RentSettingsRepository rentSettingsRepository;
    private final UserRepository userRepository;
    private final CompanyDocumentRepository companyDocumentRepository;
    private final FileByteCache fileByteCache;

    // ---------------------------------------------------------------------
    // Şirketler
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public SirketleriListeleResponseDTO sirketleriListele(SirketleriListeleRequestDTO request) {
        Pageable pageable = request.toPageable(MAX_PAGE_SIZE);
        String search = request.getData() != null ? request.getData().getSearch() : null;
        Page<Company> page = (search == null || search.isBlank())
                ? companyRepository.findAll(pageable)
                : companyRepository.findByNameContainingIgnoreCase(search, pageable);

        SirketleriListeleResponseDTO responseDTO = new SirketleriListeleResponseDTO();
        responseDTO.loadFrom(page, this::toSirketOzetDTO);
        return responseDTO;
    }

    @Transactional(readOnly = true)
    public SirketDetayGetirResponseDTO sirketDetayGetir(Long companyId) {
        Company company = getCompanyOrThrow(companyId);

        List<UyeOzetDTO> members = companyMembershipRepository.findByCompanyAndDeletedFalse(company).stream()
                .map(m -> UyeOzetDTO.builder()
                        .userId(m.getUser().getId())
                        .email(m.getUser().getEmail())
                        .role(m.getUser().getRole())
                        .companyRole(m.getCompanyRole())
                        .build())
                .toList();

        SirketDetayDTO detay = SirketDetayDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .taxNumber(company.getTaxNumber())
                .sector(company.getSector())
                .address(company.getAddress())
                .contactEmail(company.getContactEmail())
                .contactPhone(company.getContactPhone())
                .active(company.isActive())
                .rentModuleEnabled(isRentModuleEnabled(company))
                .members(members)
                .build();

        SirketDetayGetirResponseDTO responseDTO = new SirketDetayGetirResponseDTO();
        responseDTO.setData(detay);
        return responseDTO;
    }

    @Transactional
    public SirketDurumGuncelleResponseDTO sirketDurumGuncelle(Long companyId, boolean active) {
        Company company = getCompanyOrThrow(companyId);
        company.setActive(active);
        companyRepository.save(company);

        SirketDurumGuncelleResponseDTO responseDTO = new SirketDurumGuncelleResponseDTO();
        responseDTO.setCompanyId(company.getId());
        responseDTO.setActive(active);
        responseDTO.setMessages(List.of(
                AppMessageUtil.create("sirket.durum.guncelleme.basarili", "Şirket durumu güncellendi", AppMessageType.SUCCESS)));
        return responseDTO;
    }

    private SirketOzetDTO toSirketOzetDTO(Company company) {
        return SirketOzetDTO.builder()
                .id(company.getId())
                .name(company.getName())
                .taxNumber(company.getTaxNumber())
                .sector(company.getSector())
                .contactEmail(company.getContactEmail())
                .contactPhone(company.getContactPhone())
                .active(company.isActive())
                .rentModuleEnabled(isRentModuleEnabled(company))
                .memberCount(companyMembershipRepository.countByCompanyAndDeletedFalse(company))
                .build();
    }

    private boolean isRentModuleEnabled(Company company) {
        return rentSettingsRepository.findByCompany(company).map(RentSettings::isEnabled).orElse(false);
    }

    private Company getCompanyOrThrow(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("sirket.bulunamadi", "Şirket bulunamadı"));
    }

    // ---------------------------------------------------------------------
    // Kullanıcılar
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public KullanicilariListeleResponseDTO kullanicilariListele(KullanicilariListeleRequestDTO request) {
        Pageable pageable = request.toPageable(MAX_PAGE_SIZE);
        KullanicilariListeleFiltreDTO filtre = request.getData();
        Page<User> page = userRepository.searchForSuperAdmin(
                filtre != null ? filtre.getCompanyId() : null,
                filtre != null ? filtre.getRole() : null,
                pageable);

        KullanicilariListeleResponseDTO responseDTO = new KullanicilariListeleResponseDTO();
        responseDTO.loadFrom(page, this::toKullaniciOzetDTO);
        return responseDTO;
    }

    @Transactional
    public KullaniciSilResponseDTO kullaniciSil(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("kullanici.bulunamadi", "Kullanıcı bulunamadı"));
        user.setDeleted(true);
        userRepository.save(user);

        KullaniciSilResponseDTO responseDTO = new KullaniciSilResponseDTO();
        responseDTO.setMessages(List.of(
                AppMessageUtil.create("kullanici.silme.basarili", "Kullanıcı silindi", AppMessageType.SUCCESS)));
        return responseDTO;
    }

    private KullaniciOzetDTO toKullaniciOzetDTO(User user) {
        String companyName = companyMembershipRepository.findFirstByUserAndDeletedFalseOrderByIdAsc(user)
                .map(m -> m.getCompany().getName())
                .orElse(null);
        return KullaniciOzetDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .companyName(companyName)
                .build();
    }

    // ---------------------------------------------------------------------
    // Şirket belgeleri
    // ---------------------------------------------------------------------

    @Transactional
    public BelgeYukleResponseDTO belgeYukle(Long companyId, String title, MultipartFile file) {
        Company company = getCompanyOrThrow(companyId);

        if (file == null || file.isEmpty()) {
            throw new ValidationServiceException("belge.dosya.zorunlu", "Dosya zorunludur");
        }
        if (file.getSize() > MAX_DOCUMENT_SIZE_BYTES) {
            throw new ValidationServiceException("belge.boyut",
                    "Belge izin verilen " + (MAX_DOCUMENT_SIZE_BYTES / (1024 * 1024)) + "MB sınırını aşıyor");
        }

        try {
            CompanyDocument document = CompanyDocument.builder()
                    .company(company)
                    .title(title)
                    .fileName(file.getOriginalFilename())
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .fileData(file.getBytes())
                    .uploadDate(LocalDateTime.now())
                    .build();
            CompanyDocument saved = companyDocumentRepository.save(document);

            BelgeYukleResponseDTO responseDTO = new BelgeYukleResponseDTO();
            responseDTO.setId(saved.getId());
            responseDTO.setMessages(List.of(
                    AppMessageUtil.create("belge.yukleme.basarili", "Belge yüklendi", AppMessageType.SUCCESS)));
            return responseDTO;
        } catch (IOException ex) {
            throw new BusinessServiceException("belge.yukleme.hatasi", "Belge yüklenirken hata oluştu");
        }
    }

    @Transactional(readOnly = true)
    public BelgeleriListeleResponseDTO belgeleriListele(Long companyId) {
        Company company = getCompanyOrThrow(companyId);
        List<BelgeDTO> data = companyDocumentRepository.findMetaByCompanyAndDeletedFalseOrderByUploadDateDesc(company).stream()
                .map(d -> BelgeDTO.builder()
                        .id(d.getId())
                        .title(d.getTitle())
                        .fileName(d.getFileName())
                        .fileType(d.getFileType())
                        .fileSize(d.getFileSize())
                        .uploadDate(d.getUploadDate())
                        .build())
                .toList();

        BelgeleriListeleResponseDTO responseDTO = new BelgeleriListeleResponseDTO();
        responseDTO.setData(data);
        return responseDTO;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> belgeIndir(Long companyId, Long belgeId) {
        Company company = getCompanyOrThrow(companyId);
        CompanyDocumentRepository.CompanyDocumentMetaView meta = companyDocumentRepository
                .findMetaByIdAndCompanyAndDeletedFalse(belgeId, company)
                .orElseThrow(() -> new ResourceNotFoundException("belge.bulunamadi", "Belge bulunamadı"));

        byte[] data = fileByteCache.get("companyDocument:" + belgeId,
                key -> companyDocumentRepository.findById(belgeId).map(CompanyDocument::getFileData).orElse(null));

        return FileResponseUtil.inline(meta.getFileName(), meta.getFileType(), data);
    }

    @Transactional
    public BelgeSilResponseDTO belgeSil(Long companyId, Long belgeId) {
        Company company = getCompanyOrThrow(companyId);
        CompanyDocument document = companyDocumentRepository.findByIdAndCompanyAndDeletedFalse(belgeId, company)
                .orElseThrow(() -> new ResourceNotFoundException("belge.bulunamadi", "Belge bulunamadı"));
        document.setDeleted(true);
        companyDocumentRepository.save(document);
        fileByteCache.evict("companyDocument:" + belgeId);

        BelgeSilResponseDTO responseDTO = new BelgeSilResponseDTO();
        responseDTO.setMessages(List.of(
                AppMessageUtil.create("belge.silme.basarili", "Belge silindi", AppMessageType.SUCCESS)));
        return responseDTO;
    }
}
