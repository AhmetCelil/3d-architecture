package com.example.commerce.adminpanel.service;

import com.example.commerce.adminpanel.dto.*;
import com.example.commerce.adminpanel.entity.*;
import com.example.commerce.adminpanel.enums.DisplayFrequency;
import com.example.commerce.adminpanel.repository.*;
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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SirketIcerikServiceImpl implements SirketIcerikService {

    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_SHORT_TEXT_LENGTH = 2000;
    private static final int MAX_LONG_TEXT_LENGTH = 4000;
    private static final int MAX_LIST_SIZE = 50;
    private static final int MAX_PAGE_SIZE = 100;
    private static final long MAX_ANNOUNCEMENT_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyValueRepository companyValueRepository;
    private final CompanyWhyUsItemRepository companyWhyUsItemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CompanyContactInfoRepository companyContactInfoRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final AnnouncementRepository announcementRepository;
    private final AuthenticationService authenticationService;

    // ---------------------------------------------------------------------
    // Hakkımızda / Ana Sayfa
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public HakkimizdaGetirResponseDTO hakkimizdaGetir() {
        Company company = authenticationService.getAuthenticatedUserCompany();
        CompanyProfile profile = companyProfileRepository.findByCompany(company).orElse(null);

        HakkimizdaGetirResponseDTO responseDTO = new HakkimizdaGetirResponseDTO();
        responseDTO.setData(profile != null ? convertToHakkimizdaDTO(profile) : HakkimizdaDTO.builder().build());
        return responseDTO;
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO hakkimizdaGuncelle(HakkimizdaGuncelleRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();

        validateTextLength(request.getAboutTitle(), MAX_TITLE_LENGTH, "Hakkımızda başlığı");
        validateTextLength(request.getAboutDescription(), MAX_LONG_TEXT_LENGTH, "Hakkımızda açıklaması");
        validateTextLength(request.getMission(), MAX_SHORT_TEXT_LENGTH, "Misyon");
        validateTextLength(request.getVision(), MAX_SHORT_TEXT_LENGTH, "Vizyon");
        validateTextLength(request.getStory(), MAX_LONG_TEXT_LENGTH, "Hikaye");
        validateTextLength(request.getHomepageTitle(), MAX_TITLE_LENGTH, "Ana sayfa başlığı");
        validateTextLength(request.getHomepageSubtitle(), MAX_SHORT_TEXT_LENGTH, "Ana sayfa açıklaması");

        CompanyProfile profile = companyProfileRepository.findByCompany(company)
                .orElseGet(() -> CompanyProfile.builder().company(company).build());

        profile.setAboutTitle(request.getAboutTitle());
        profile.setAboutDescription(request.getAboutDescription());
        profile.setMission(request.getMission());
        profile.setVision(request.getVision());
        profile.setFoundedYear(request.getFoundedYear());
        profile.setStory(request.getStory());
        profile.setHomepageTitle(request.getHomepageTitle());
        profile.setHomepageSubtitle(request.getHomepageSubtitle());
        profile.setCompletedProjectsCount(request.getCompletedProjectsCount());
        profile.setOngoingProjectsCount(request.getOngoingProjectsCount());
        profile.setExperienceYears(request.getExperienceYears());
        profile.setHappyClientsCount(request.getHappyClientsCount());

        companyProfileRepository.save(profile);
        log.info("Hakkımızda/Ana sayfa profili güncellendi: company={}", company.getId());

        return ackResponse("hakkimizda.guncelleme.basarili", "Hakkımızda bilgileri güncellendi");
    }

    private HakkimizdaDTO convertToHakkimizdaDTO(CompanyProfile profile) {
        return HakkimizdaDTO.builder()
                .aboutTitle(profile.getAboutTitle())
                .aboutDescription(profile.getAboutDescription())
                .mission(profile.getMission())
                .vision(profile.getVision())
                .foundedYear(profile.getFoundedYear())
                .story(profile.getStory())
                .homepageTitle(profile.getHomepageTitle())
                .homepageSubtitle(profile.getHomepageSubtitle())
                .completedProjectsCount(profile.getCompletedProjectsCount())
                .ongoingProjectsCount(profile.getOngoingProjectsCount())
                .experienceYears(profile.getExperienceYears())
                .happyClientsCount(profile.getHappyClientsCount())
                .build();
    }

    // ---------------------------------------------------------------------
    // Değerlerimiz
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DegerlerListeleResponseDTO degerleriListele() {
        Company company = authenticationService.getAuthenticatedUserCompany();
        List<CompanyValue> values = companyValueRepository.findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(company);

        DegerlerListeleResponseDTO responseDTO = new DegerlerListeleResponseDTO();
        responseDTO.setData(values.stream().map(this::convertToDegerDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public SiteContentIdResponseDTO degerEkle(DegerKaydetRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateBasliklOgeRequest(request.getTitle(), request.getDescription());

        CompanyValue value = CompanyValue.builder()
                .company(company)
                .title(request.getTitle())
                .description(request.getDescription())
                .iconKey(request.getIconKey())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        CompanyValue saved = companyValueRepository.save(value);

        return idResponse(saved.getId(), "deger.ekleme.basarili", "Değer eklendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO degerGuncelle(Long id, DegerKaydetRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateBasliklOgeRequest(request.getTitle(), request.getDescription());

        CompanyValue value = companyValueRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("deger.bulunamadi", "Değer bulunamadı"));

        value.setTitle(request.getTitle());
        value.setDescription(request.getDescription());
        value.setIconKey(request.getIconKey());
        if (request.getDisplayOrder() != null) {
            value.setDisplayOrder(request.getDisplayOrder());
        }
        companyValueRepository.save(value);

        return ackResponse("deger.guncelleme.basarili", "Değer güncellendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO degerSil(Long id) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        CompanyValue value = companyValueRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("deger.bulunamadi", "Değer bulunamadı"));

        value.setDeleted(true);
        companyValueRepository.save(value);

        return ackResponse("deger.silme.basarili", "Değer silindi");
    }

    private DegerDTO convertToDegerDTO(CompanyValue value) {
        return DegerDTO.builder()
                .id(value.getId())
                .title(value.getTitle())
                .description(value.getDescription())
                .iconKey(value.getIconKey())
                .displayOrder(value.getDisplayOrder())
                .build();
    }

    // ---------------------------------------------------------------------
    // Neden Bizi Seçmelisiniz
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public NedenBizlerListeleResponseDTO nedenBizleriListele() {
        Company company = authenticationService.getAuthenticatedUserCompany();
        List<CompanyWhyUsItem> items = companyWhyUsItemRepository.findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(company);

        NedenBizlerListeleResponseDTO responseDTO = new NedenBizlerListeleResponseDTO();
        responseDTO.setData(items.stream().map(this::convertToNedenBizDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public SiteContentIdResponseDTO nedenBizEkle(NedenBizKaydetRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateBasliklOgeRequest(request.getTitle(), request.getDescription());

        CompanyWhyUsItem item = CompanyWhyUsItem.builder()
                .company(company)
                .title(request.getTitle())
                .description(request.getDescription())
                .iconKey(request.getIconKey())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();
        CompanyWhyUsItem saved = companyWhyUsItemRepository.save(item);

        return idResponse(saved.getId(), "nedenbiz.ekleme.basarili", "Neden biz öğesi eklendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO nedenBizGuncelle(Long id, NedenBizKaydetRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateBasliklOgeRequest(request.getTitle(), request.getDescription());

        CompanyWhyUsItem item = companyWhyUsItemRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("nedenbiz.bulunamadi", "Neden biz öğesi bulunamadı"));

        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setIconKey(request.getIconKey());
        if (request.getDisplayOrder() != null) {
            item.setDisplayOrder(request.getDisplayOrder());
        }
        companyWhyUsItemRepository.save(item);

        return ackResponse("nedenbiz.guncelleme.basarili", "Neden biz öğesi güncellendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO nedenBizSil(Long id) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        CompanyWhyUsItem item = companyWhyUsItemRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("nedenbiz.bulunamadi", "Neden biz öğesi bulunamadı"));

        item.setDeleted(true);
        companyWhyUsItemRepository.save(item);

        return ackResponse("nedenbiz.silme.basarili", "Neden biz öğesi silindi");
    }

    private NedenBizDTO convertToNedenBizDTO(CompanyWhyUsItem item) {
        return NedenBizDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .iconKey(item.getIconKey())
                .displayOrder(item.getDisplayOrder())
                .build();
    }

    // ---------------------------------------------------------------------
    // Ekibimiz
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public EkipUyeleriListeleResponseDTO ekipUyeleriniListele() {
        Company company = authenticationService.getAuthenticatedUserCompany();
        List<TeamMember> members = teamMemberRepository.findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(company);

        EkipUyeleriListeleResponseDTO responseDTO = new EkipUyeleriListeleResponseDTO();
        responseDTO.setData(members.stream().map(this::convertToEkipUyesiDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public SiteContentIdResponseDTO ekipUyesiEkle(EkipUyesiKaydetRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateEkipUyesiRequest(request);

        TeamMember member = TeamMember.builder()
                .company(company)
                .fullName(request.getFullName())
                .title(request.getTitle())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .active(request.getActive() == null || request.getActive())
                .build();
        TeamMember saved = teamMemberRepository.save(member);

        return idResponse(saved.getId(), "ekip.ekleme.basarili", "Ekip üyesi eklendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO ekipUyesiGuncelle(Long id, EkipUyesiKaydetRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateEkipUyesiRequest(request);

        TeamMember member = teamMemberRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("ekip.bulunamadi", "Ekip üyesi bulunamadı"));

        member.setFullName(request.getFullName());
        member.setTitle(request.getTitle());
        member.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) {
            member.setDisplayOrder(request.getDisplayOrder());
        }
        if (request.getActive() != null) {
            member.setActive(request.getActive());
        }
        teamMemberRepository.save(member);

        return ackResponse("ekip.guncelleme.basarili", "Ekip üyesi güncellendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO ekipUyesiSil(Long id) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        TeamMember member = teamMemberRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("ekip.bulunamadi", "Ekip üyesi bulunamadı"));

        member.setDeleted(true);
        teamMemberRepository.save(member);

        return ackResponse("ekip.silme.basarili", "Ekip üyesi silindi");
    }

    private void validateEkipUyesiRequest(EkipUyesiKaydetRequestDTO request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ValidationServiceException("ekip.ad.zorunlu", "Ekip üyesi adı zorunludur");
        }
        validateTextLength(request.getFullName(), MAX_TITLE_LENGTH, "Ad Soyad");
        validateTextLength(request.getTitle(), MAX_TITLE_LENGTH, "Unvan");
        validateTextLength(request.getDescription(), MAX_SHORT_TEXT_LENGTH, "Açıklama");
    }

    private EkipUyesiDTO convertToEkipUyesiDTO(TeamMember member) {
        return EkipUyesiDTO.builder()
                .id(member.getId())
                .fullName(member.getFullName())
                .title(member.getTitle())
                .description(member.getDescription())
                .displayOrder(member.getDisplayOrder())
                .active(member.isActive())
                .build();
    }

    // ---------------------------------------------------------------------
    // İletişim Bilgileri
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public IletisimBilgileriGetirResponseDTO iletisimBilgileriGetir() {
        Company company = authenticationService.getAuthenticatedUserCompany();
        CompanyContactInfo contactInfo = companyContactInfoRepository.findByCompany(company).orElse(null);

        IletisimBilgileriGetirResponseDTO responseDTO = new IletisimBilgileriGetirResponseDTO();
        responseDTO.setData(contactInfo != null ? convertToIletisimBilgileriDTO(contactInfo) : IletisimBilgileriDTO.builder().workingHours(List.of()).build());
        return responseDTO;
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO iletisimBilgileriGuncelle(IletisimBilgileriGuncelleRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();

        validateTextLength(request.getAddress(), MAX_SHORT_TEXT_LENGTH, "Adres");
        validateTextLength(request.getEmail(), MAX_TITLE_LENGTH, "E-posta");
        if (request.getWorkingHours() != null && request.getWorkingHours().size() > 7) {
            throw new ValidationServiceException("iletisim.calismasaati.limit", "Çalışma saatleri en fazla 7 gün içerebilir");
        }

        CompanyContactInfo contactInfo = companyContactInfoRepository.findByCompany(company)
                .orElseGet(() -> CompanyContactInfo.builder().company(company).build());

        contactInfo.setAddress(request.getAddress());
        contactInfo.setPhone(request.getPhone());
        contactInfo.setPhoneSecondary(request.getPhoneSecondary());
        contactInfo.setEmail(request.getEmail());
        contactInfo.setWhatsappNumber(request.getWhatsappNumber());
        contactInfo.setInstagramUrl(request.getInstagramUrl());
        contactInfo.setTwitterUrl(request.getTwitterUrl());
        contactInfo.setFacebookUrl(request.getFacebookUrl());
        contactInfo.setLinkedinUrl(request.getLinkedinUrl());
        contactInfo.setYoutubeUrl(request.getYoutubeUrl());
        contactInfo.setMapLatitude(request.getMapLatitude());
        contactInfo.setMapLongitude(request.getMapLongitude());
        contactInfo.setMapEmbedUrl(request.getMapEmbedUrl());
        // workingHours Hibernate tarafından izlenen bir @ElementCollection; alanı yeni bir
        // liste ile değiştirmek (setWorkingHours) mevcut kayıtlarda izlenen PersistentBag'i
        // sahibinden koparıp "no longer referenced" HibernateException'ına (500) yol açıyordu.
        // Mevcut koleksiyonu yerinde temizleyip doldurmak Hibernate'in aynı bag'i izlemeye
        // devam etmesini sağlıyor.
        contactInfo.getWorkingHours().clear();
        if (request.getWorkingHours() != null) {
            contactInfo.getWorkingHours().addAll(
                    request.getWorkingHours().stream().map(this::toWorkingHourEntry).toList());
        }

        companyContactInfoRepository.save(contactInfo);
        log.info("İletişim bilgileri güncellendi: company={}", company.getId());

        return ackResponse("iletisim.guncelleme.basarili", "İletişim bilgileri güncellendi");
    }

    private WorkingHourEntry toWorkingHourEntry(CalismaSaatiDTO dto) {
        return WorkingHourEntry.builder()
                .dayOfWeek(dto.getDayOfWeek())
                .opensAt(dto.getOpensAt())
                .closesAt(dto.getClosesAt())
                .closed(dto.isClosed())
                .build();
    }

    private IletisimBilgileriDTO convertToIletisimBilgileriDTO(CompanyContactInfo contactInfo) {
        return IletisimBilgileriDTO.builder()
                .address(contactInfo.getAddress())
                .phone(contactInfo.getPhone())
                .phoneSecondary(contactInfo.getPhoneSecondary())
                .email(contactInfo.getEmail())
                .whatsappNumber(contactInfo.getWhatsappNumber())
                .instagramUrl(contactInfo.getInstagramUrl())
                .twitterUrl(contactInfo.getTwitterUrl())
                .facebookUrl(contactInfo.getFacebookUrl())
                .linkedinUrl(contactInfo.getLinkedinUrl())
                .youtubeUrl(contactInfo.getYoutubeUrl())
                .mapLatitude(contactInfo.getMapLatitude())
                .mapLongitude(contactInfo.getMapLongitude())
                .mapEmbedUrl(contactInfo.getMapEmbedUrl())
                .workingHours(contactInfo.getWorkingHours().stream()
                        .map(wh -> CalismaSaatiDTO.builder()
                                .dayOfWeek(wh.getDayOfWeek())
                                .opensAt(wh.getOpensAt())
                                .closesAt(wh.getClosesAt())
                                .closed(wh.isClosed())
                                .build())
                        .toList())
                .build();
    }

    // ---------------------------------------------------------------------
    // İletişim Mesajları (gelen kutusu)
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public IletisimMesajlariListeleResponseDTO iletisimMesajlariniListele(IletisimMesajlariListeleRequestDTO request) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        Pageable pageable = request.toPageable(MAX_PAGE_SIZE);

        Page<ContactMessage> messages = request.getRead() != null
                ? contactMessageRepository.findByCompanyAndDeletedFalseAndReadOrderByCreatedAtDesc(company, request.getRead(), pageable)
                : contactMessageRepository.findByCompanyAndDeletedFalseOrderByCreatedAtDesc(company, pageable);

        IletisimMesajlariListeleResponseDTO responseDTO = new IletisimMesajlariListeleResponseDTO();
        responseDTO.loadFrom(messages, this::convertToIletisimMesajiDTO);
        return responseDTO;
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO iletisimMesajiOkunduIsaretle(Long id) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        ContactMessage message = contactMessageRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("mesaj.bulunamadi", "Mesaj bulunamadı"));

        message.setRead(true);
        contactMessageRepository.save(message);

        return ackResponse("mesaj.okundu.basarili", "Mesaj okundu olarak işaretlendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO iletisimMesajiSil(Long id) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        ContactMessage message = contactMessageRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("mesaj.bulunamadi", "Mesaj bulunamadı"));

        message.setDeleted(true);
        contactMessageRepository.save(message);

        return ackResponse("mesaj.silme.basarili", "Mesaj silindi");
    }

    private IletisimMesajiDTO convertToIletisimMesajiDTO(ContactMessage message) {
        return IletisimMesajiDTO.builder()
                .id(message.getId())
                .fullName(message.getFullName())
                .email(message.getEmail())
                .phone(message.getPhone())
                .subject(message.getSubject())
                .message(message.getMessage())
                .read(message.isRead())
                .createdAt(message.getCreatedAt())
                .build();
    }

    // ---------------------------------------------------------------------
    // Duyuru / Pop-up
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public DuyurulariListeleResponseDTO duyurulariListele() {
        Company company = authenticationService.getAuthenticatedUserCompany();
        List<Announcement> announcements = announcementRepository.findByCompanyAndDeletedFalseOrderByPriorityDescCreatedAtDesc(company);

        DuyurulariListeleResponseDTO responseDTO = new DuyurulariListeleResponseDTO();
        responseDTO.setData(announcements.stream().map(this::convertToDuyuruDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public SiteContentIdResponseDTO duyuruEkle(DuyuruKaydetRequestDTO request, MultipartFile image) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateDuyuruRequest(request);

        Announcement announcement = Announcement.builder()
                .company(company)
                .title(request.getTitle())
                .message(request.getMessage())
                .announcementType(request.getAnnouncementType())
                .linkUrl(request.getLinkUrl())
                .buttonText(request.getButtonText())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() == null || request.getActive())
                .priority(request.getPriority() != null ? request.getPriority() : 0)
                .displayFrequency(request.getDisplayFrequency() != null ? request.getDisplayFrequency() : DisplayFrequency.HER_ZAMAN)
                .build();

        applyImage(announcement, image);

        Announcement saved = announcementRepository.save(announcement);
        log.info("Duyuru eklendi: id={}, company={}", saved.getId(), company.getId());

        return idResponse(saved.getId(), "duyuru.ekleme.basarili", "Duyuru eklendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO duyuruGuncelle(Long id, DuyuruKaydetRequestDTO request, MultipartFile image) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        validateDuyuruRequest(request);

        Announcement announcement = announcementRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("duyuru.bulunamadi", "Duyuru bulunamadı"));

        announcement.setTitle(request.getTitle());
        announcement.setMessage(request.getMessage());
        announcement.setAnnouncementType(request.getAnnouncementType());
        announcement.setLinkUrl(request.getLinkUrl());
        announcement.setButtonText(request.getButtonText());
        announcement.setStartDate(request.getStartDate());
        announcement.setEndDate(request.getEndDate());
        if (request.getActive() != null) {
            announcement.setActive(request.getActive());
        }
        if (request.getPriority() != null) {
            announcement.setPriority(request.getPriority());
        }
        if (request.getDisplayFrequency() != null) {
            announcement.setDisplayFrequency(request.getDisplayFrequency());
        }

        if (image != null && !image.isEmpty()) {
            applyImage(announcement, image);
        } else if (request.isRemoveImage()) {
            announcement.setImageData(null);
            announcement.setImageFileName(null);
            announcement.setImageFileType(null);
        }

        announcementRepository.save(announcement);
        log.info("Duyuru güncellendi: id={}, company={}", announcement.getId(), company.getId());

        return ackResponse("duyuru.guncelleme.basarili", "Duyuru güncellendi");
    }

    @Override
    @Transactional
    public SiteContentAckResponseDTO duyuruSil(Long id) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        Announcement announcement = announcementRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("duyuru.bulunamadi", "Duyuru bulunamadı"));

        announcement.setDeleted(true);
        announcementRepository.save(announcement);

        return ackResponse("duyuru.silme.basarili", "Duyuru silindi");
    }

    @Override
    @Transactional(readOnly = true)
    public DuyuruGorsel duyuruGorseliGetir(Long id) {
        Company company = authenticationService.getAuthenticatedUserCompany();
        Announcement announcement = announcementRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("duyuru.bulunamadi", "Duyuru bulunamadı"));

        if (announcement.getImageData() == null) {
            throw new ResourceNotFoundException("duyuru.gorsel.bulunamadi", "Duyuruya ait görsel bulunamadı");
        }

        return new DuyuruGorsel(announcement.getImageFileName(), announcement.getImageFileType(), announcement.getImageData());
    }

    private void applyImage(Announcement announcement, MultipartFile image) {
        if (image == null || image.isEmpty()) return;
        if (image.getSize() > MAX_ANNOUNCEMENT_IMAGE_SIZE_BYTES) {
            throw new ValidationServiceException("duyuru.gorsel.boyut",
                    "Duyuru görseli izin verilen " + (MAX_ANNOUNCEMENT_IMAGE_SIZE_BYTES / (1024 * 1024)) + "MB sınırını aşıyor");
        }
        try {
            announcement.setImageData(image.getBytes());
            announcement.setImageFileName(image.getOriginalFilename());
            announcement.setImageFileType(image.getContentType());
        } catch (IOException ex) {
            log.error("Duyuru görseli işlenirken hata: {}", ex.getMessage(), ex);
            throw new BusinessServiceException("duyuru.gorsel.hatasi", "Duyuru görseli yüklenirken hata oluştu");
        }
    }

    private void validateDuyuruRequest(DuyuruKaydetRequestDTO request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ValidationServiceException("duyuru.baslik.zorunlu", "Duyuru başlığı zorunludur");
        }
        if (request.getAnnouncementType() == null) {
            throw new ValidationServiceException("duyuru.tur.zorunlu", "Duyuru türü zorunludur");
        }
        validateTextLength(request.getTitle(), MAX_TITLE_LENGTH, "Duyuru başlığı");
        validateTextLength(request.getMessage(), MAX_SHORT_TEXT_LENGTH, "Duyuru mesajı");
        if (request.getStartDate() != null && request.getEndDate() != null && request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationServiceException("duyuru.tarih.gecersiz", "Bitiş tarihi başlangıç tarihinden önce olamaz");
        }
    }

    private DuyuruDTO convertToDuyuruDTO(Announcement announcement) {
        return DuyuruDTO.builder()
                .id(announcement.getId())
                .title(announcement.getTitle())
                .message(announcement.getMessage())
                .announcementType(announcement.getAnnouncementType())
                .hasImage(announcement.getImageData() != null)
                .linkUrl(announcement.getLinkUrl())
                .buttonText(announcement.getButtonText())
                .startDate(announcement.getStartDate())
                .endDate(announcement.getEndDate())
                .active(announcement.isActive())
                .priority(announcement.getPriority())
                .displayFrequency(announcement.getDisplayFrequency())
                .createdAt(announcement.getCreatedAt())
                .build();
    }

    // ---------------------------------------------------------------------
    // Ortak yardımcılar
    // ---------------------------------------------------------------------

    private void validateBasliklOgeRequest(String title, String description) {
        if (title == null || title.isBlank()) {
            throw new ValidationServiceException("oge.baslik.zorunlu", "Başlık zorunludur");
        }
        validateTextLength(title, MAX_TITLE_LENGTH, "Başlık");
        validateTextLength(description, MAX_SHORT_TEXT_LENGTH, "Açıklama");
    }

    private void validateTextLength(String value, int maxLength, String fieldLabel) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationServiceException("icerik.metin.uzunluk",
                    fieldLabel + " en fazla " + maxLength + " karakter olabilir");
        }
    }

    private SiteContentAckResponseDTO ackResponse(String code, String text) {
        SiteContentAckResponseDTO responseDTO = new SiteContentAckResponseDTO();
        responseDTO.setMessages(List.of(AppMessageUtil.create(code, text, AppMessageType.SUCCESS)));
        return responseDTO;
    }

    private SiteContentIdResponseDTO idResponse(Long id, String code, String text) {
        SiteContentIdResponseDTO responseDTO = new SiteContentIdResponseDTO();
        responseDTO.setId(id);
        responseDTO.setMessages(List.of(AppMessageUtil.create(code, text, AppMessageType.SUCCESS)));
        return responseDTO;
    }
}
