package com.example.commerce.publicapi.service;

import com.example.commerce.adminpanel.entity.Announcement;
import com.example.commerce.adminpanel.entity.CompanyContactInfo;
import com.example.commerce.adminpanel.entity.CompanyProfile;
import com.example.commerce.adminpanel.entity.ContactMessage;
import com.example.commerce.adminpanel.repository.*;
import com.example.commerce.common.cache.FileByteCache;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.publicapi.dto.*;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.tenant.service.ApiKeyService;
import com.example.commerce.util.FileResponseUtil;
import com.example.commerce.whatsapp.service.WhatsAppService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PublicSiteService {

    private static final int MAX_FULL_NAME_LENGTH = 255;
    private static final int MAX_MESSAGE_LENGTH = 4000;

    private final ApiKeyService apiKeyService;
    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyValueRepository companyValueRepository;
    private final CompanyWhyUsItemRepository companyWhyUsItemRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CompanyContactInfoRepository companyContactInfoRepository;
    private final AnnouncementRepository announcementRepository;
    private final ContactMessageRepository contactMessageRepository;
    private final WhatsAppService whatsAppService;
    private final FileByteCache fileByteCache;

    private Company getCompanyByApiKey(String apiKey) {
        return apiKeyService.resolveCompany(apiKey);
    }

    @Transactional
    public PublicHakkimizdaResponseDTO hakkimizdaGetir(String apiKey) {
        Company company = getCompanyByApiKey(apiKey);
        CompanyProfile profile = companyProfileRepository.findByCompany(company).orElse(null);

        PublicHakkimizdaDTO data = PublicHakkimizdaDTO.builder()
                .aboutTitle(profile != null ? profile.getAboutTitle() : null)
                .aboutDescription(profile != null ? profile.getAboutDescription() : null)
                .mission(profile != null ? profile.getMission() : null)
                .vision(profile != null ? profile.getVision() : null)
                .foundedYear(profile != null ? profile.getFoundedYear() : null)
                .story(profile != null ? profile.getStory() : null)
                .values(companyValueRepository.findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(company).stream()
                        .map(v -> PublicDegerDTO.builder().title(v.getTitle()).description(v.getDescription()).iconKey(v.getIconKey()).build())
                        .toList())
                .whyUs(companyWhyUsItemRepository.findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(company).stream()
                        .map(w -> PublicNedenBizDTO.builder().title(w.getTitle()).description(w.getDescription()).iconKey(w.getIconKey()).build())
                        .toList())
                .team(teamMemberRepository.findByCompanyAndDeletedFalseAndActiveTrueOrderByDisplayOrderAsc(company).stream()
                        .map(t -> PublicEkipUyesiDTO.builder().fullName(t.getFullName()).title(t.getTitle()).description(t.getDescription()).build())
                        .toList())
                .build();

        PublicHakkimizdaResponseDTO responseDTO = new PublicHakkimizdaResponseDTO();
        responseDTO.setData(data);
        return responseDTO;
    }

    @Transactional
    public PublicIletisimResponseDTO iletisimGetir(String apiKey) {
        Company company = getCompanyByApiKey(apiKey);
        CompanyContactInfo contactInfo = companyContactInfoRepository.findByCompany(company).orElse(null);

        PublicIletisimDTO data = contactInfo == null
                ? PublicIletisimDTO.builder().workingHours(List.of()).build()
                : PublicIletisimDTO.builder()
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
                            .map(wh -> PublicCalismaSaatiDTO.builder()
                                    .dayOfWeek(wh.getDayOfWeek() != null ? wh.getDayOfWeek().name() : null)
                                    .opensAt(wh.getOpensAt())
                                    .closesAt(wh.getClosesAt())
                                    .closed(wh.isClosed())
                                    .build())
                            .toList())
                    .build();

        PublicIletisimResponseDTO responseDTO = new PublicIletisimResponseDTO();
        responseDTO.setData(data);
        return responseDTO;
    }

    @Transactional
    public PublicAnasayfaResponseDTO anasayfaGetir(String apiKey) {
        Company company = getCompanyByApiKey(apiKey);
        CompanyProfile profile = companyProfileRepository.findByCompany(company).orElse(null);

        PublicAnasayfaDTO data = PublicAnasayfaDTO.builder()
                .homepageTitle(profile != null ? profile.getHomepageTitle() : null)
                .homepageSubtitle(profile != null ? profile.getHomepageSubtitle() : null)
                .completedProjectsCount(profile != null ? profile.getCompletedProjectsCount() : null)
                .ongoingProjectsCount(profile != null ? profile.getOngoingProjectsCount() : null)
                .experienceYears(profile != null ? profile.getExperienceYears() : null)
                .happyClientsCount(profile != null ? profile.getHappyClientsCount() : null)
                .build();

        PublicAnasayfaResponseDTO responseDTO = new PublicAnasayfaResponseDTO();
        responseDTO.setData(data);
        return responseDTO;
    }

    @Transactional
    public PublicDuyurularResponseDTO aktifDuyurulariGetir(String apiKey) {
        Company company = getCompanyByApiKey(apiKey);
        LocalDateTime now = LocalDateTime.now();

        List<PublicDuyuruDTO> data = announcementRepository.findMetaByCompanyAndDeletedFalseOrderByPriorityDescCreatedAtDesc(company).stream()
                .filter(AnnouncementRepository.AnnouncementMetaView::isActive)
                .filter(a -> a.getStartDate() == null || !a.getStartDate().isAfter(now))
                .filter(a -> a.getEndDate() == null || !a.getEndDate().isBefore(now))
                .map(a -> PublicDuyuruDTO.builder()
                        .id(a.getId())
                        .title(a.getTitle())
                        .message(a.getMessage())
                        .announcementType(a.getAnnouncementType() != null ? a.getAnnouncementType().name() : null)
                        .hasImage(a.getImageFileName() != null)
                        .linkUrl(a.getLinkUrl())
                        .buttonText(a.getButtonText())
                        .priority(a.getPriority())
                        .displayFrequency(a.getDisplayFrequency() != null ? a.getDisplayFrequency().name() : null)
                        .startDate(a.getStartDate())
                        .endDate(a.getEndDate())
                        .build())
                .toList();

        PublicDuyurularResponseDTO responseDTO = new PublicDuyurularResponseDTO();
        responseDTO.setData(data);
        return responseDTO;
    }

    @Transactional
    public ResponseEntity<byte[]> duyuruGorseliGetir(String apiKey, Long duyuruId) {
        Company company = getCompanyByApiKey(apiKey);

        AnnouncementRepository.AnnouncementImageMetaView meta = announcementRepository
                .findImageMetaByIdAndCompanyAndDeletedFalse(duyuruId, company)
                .orElseThrow(() -> new ResourceNotFoundException("duyuru.bulunamadi", "Duyuru bulunamadı"));

        if (meta.getImageFileName() == null) {
            throw new ResourceNotFoundException("duyuru.gorsel.bulunamadi", "Duyuruya ait görsel bulunamadı");
        }

        byte[] data = fileByteCache.get("announcementImage:" + duyuruId,
                key -> announcementRepository.findById(duyuruId).map(Announcement::getImageData).orElse(null));

        return FileResponseUtil.inline(meta.getImageFileName(), meta.getImageFileType(), data, "private, max-age=3600");
    }

    @Transactional
    public PublicIletisimFormuResponseDTO iletisimFormuGonder(String apiKey, PublicIletisimFormuRequestDTO request) {
        Company company = getCompanyByApiKey(apiKey);
        validateIletisimFormuRequest(request);

        ContactMessage message = ContactMessage.builder()
                .company(company)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .subject(request.getSubject())
                .message(request.getMessage())
                .build();
        contactMessageRepository.save(message);
        log.info("Yeni iletişim formu mesajı alındı: company={}", company.getId());

        companyContactInfoRepository.findByCompany(company).ifPresent(contactInfo -> {
            String text = String.format(
                    "*📩 Yeni İletişim Formu Mesajı*\n\n" +
                            "*Ad Soyad:* %s\n" +
                            "*E-posta:* %s\n" +
                            "*Telefon:* %s\n" +
                            "*Konu:* %s\n" +
                            "*Mesaj:* %s",
                    request.getFullName(), request.getEmail(), nz(request.getPhone()),
                    nz(request.getSubject()), request.getMessage());
            whatsAppService.sendMessage(contactInfo.getWhatsappNumber(), contactInfo.getWhatsappApiKey(), text);
        });

        return new PublicIletisimFormuResponseDTO();
    }

    private static String nz(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private void validateIletisimFormuRequest(PublicIletisimFormuRequestDTO request) {
        if (request.getFullName() == null || request.getFullName().isBlank()) {
            throw new ValidationServiceException("iletisimformu.ad.zorunlu", "Ad Soyad zorunludur");
        }
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new ValidationServiceException("iletisimformu.eposta.zorunlu", "E-posta zorunludur");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ValidationServiceException("iletisimformu.mesaj.zorunlu", "Mesaj zorunludur");
        }
        if (request.getFullName().length() > MAX_FULL_NAME_LENGTH) {
            throw new ValidationServiceException("iletisimformu.ad.uzunluk", "Ad Soyad en fazla " + MAX_FULL_NAME_LENGTH + " karakter olabilir");
        }
        if (request.getMessage().length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationServiceException("iletisimformu.mesaj.uzunluk", "Mesaj en fazla " + MAX_MESSAGE_LENGTH + " karakter olabilir");
        }
    }
}
