package com.example.commerce.rent.rentpublic.service;

import com.example.commerce.adminpanel.repository.CompanyContactInfoRepository;
import com.example.commerce.altcha.AltchaService;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.mail.service.MailService;
import com.example.commerce.whatsapp.service.WhatsAppService;
import com.example.commerce.rent.entity.RentSettings;
import com.example.commerce.rent.entity.Villa;
import com.example.commerce.rent.entity.VillaImage;
import com.example.commerce.rent.entity.VillaReservation;
import com.example.commerce.rent.enums.ReservationStatus;
import com.example.commerce.rent.repository.RentSettingsRepository;
import com.example.commerce.rent.repository.VillaImageRepository;
import com.example.commerce.rent.repository.VillaRepository;
import com.example.commerce.rent.repository.VillaReservationRepository;
import com.example.commerce.rent.rentpublic.dto.*;
import com.example.commerce.rent.service.RentCalendarService;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.tenant.repository.CompanyRepository;
import com.example.commerce.tenant.service.ApiKeyService;
import com.example.commerce.util.AppMessageUtil;
import com.example.commerce.util.FileResponseUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentPublicServiceImpl implements RentPublicService {

    private final VillaRepository villaRepository;
    private final VillaImageRepository villaImageRepository;
    private final VillaReservationRepository villaReservationRepository;
    private final RentSettingsRepository rentSettingsRepository;
    private final CompanyRepository companyRepository;
    private final ApiKeyService apiKeyService;
    private final AltchaService altchaService;
    private final MailService mailService;
    private final WhatsAppService whatsAppService;
    private final CompanyContactInfoRepository companyContactInfoRepository;
    private final RentCalendarService calendarService;

    @Override
    @Transactional(readOnly = true)
    public PublicVillalarResponseDTO villalariListele(String apiKey) {
        Company company = requireModuleEnabled(apiKey);

        PublicVillalarResponseDTO responseDTO = new PublicVillalarResponseDTO();
        responseDTO.setData(villaRepository.findByCompanyAndDeletedFalseAndActiveTrueOrderByNameAsc(company).stream()
                .map(this::convertToPublicVillaDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicVillalarResponseDTO musaitVillalariAra(String apiKey, LocalDate start, LocalDate end) {
        Company company = requireModuleEnabled(apiKey);
        validateDateRange(start, end);

        PublicVillalarResponseDTO responseDTO = new PublicVillalarResponseDTO();
        responseDTO.setData(calendarService.availableVillas(company, start, end).stream()
                .map(this::convertToPublicVillaDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicVillaDetayResponseDTO villaDetay(String apiKey, Long villaId) {
        Company company = requireModuleEnabled(apiKey);
        Villa villa = findVillaOrThrow(villaId, company);

        PublicVillaDetayResponseDTO responseDTO = new PublicVillaDetayResponseDTO();
        responseDTO.setData(convertToPublicVillaDTO(villa));
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<byte[]> gorselGetir(String apiKey, Long villaId, Long imageId) {
        Company company = requireModuleEnabled(apiKey);
        Villa villa = findVillaOrThrow(villaId, company);
        VillaImage image = villaImageRepository.findByIdAndVillaAndDeletedFalse(imageId, villa)
                .orElseThrow(() -> new ResourceNotFoundException("villa.gorsel.bulunamadi", "Villa görseli bulunamadı"));

        return FileResponseUtil.inline(image.getFileName(), image.getFileType(), image.getFileData());
    }

    @Override
    @Transactional(readOnly = true)
    public PublicMusaitlikResponseDTO musaitlik(String apiKey, Long villaId, LocalDate start, LocalDate end) {
        Company company = requireModuleEnabled(apiKey);
        Villa villa = findVillaOrThrow(villaId, company);
        validateDateRange(start, end);

        PublicMusaitlikResponseDTO responseDTO = new PublicMusaitlikResponseDTO();
        responseDTO.setDoluAraliklar(calendarService.blockedRanges(villa, start, end).stream()
                .map(r -> PublicMusaitAralikDTO.builder().startDate(r.startDate()).endDate(r.endDate()).build())
                .toList());
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PublicVillaFiyatResponseDTO fiyat(String apiKey, Long villaId, LocalDate start, LocalDate end) {
        Company company = requireModuleEnabled(apiKey);
        Villa villa = findVillaOrThrow(villaId, company);
        validateDateRange(start, end);

        PublicVillaFiyatResponseDTO responseDTO = new PublicVillaFiyatResponseDTO();
        responseDTO.setPriceVisible(villa.isPriceVisible() && villa.getPrice() != null);
        if (responseDTO.isPriceVisible()) {
            List<RentCalendarService.DateRange> doluAraliklar = calendarService.blockedRanges(villa, start, end);

            List<PublicGunlukFiyatDTO> gunlukFiyatlar = calendarService.dailyPrices(villa, start, end).stream()
                    .map(dp -> PublicGunlukFiyatDTO.builder()
                            .date(dp.date())
                            .price(dp.price())
                            .specialPrice(dp.special())
                            .available(doluAraliklar.stream().noneMatch(r -> !dp.date().isBefore(r.startDate()) && dp.date().isBefore(r.endDate())))
                            .build())
                    .toList();
            responseDTO.setGunlukFiyatlar(gunlukFiyatlar);
        }
        return responseDTO;
    }

    @Override
    @Transactional
    public PublicVillaRezervasyonTalebiResponseDTO rezervasyonTalebiOlustur(String apiKey, Long villaId, PublicVillaRezervasyonTalebiRequestDTO request) {
        Company company = requireModuleEnabled(apiKey);
        Villa villa = findVillaOrThrow(villaId, company);

        if (!altchaService.verify(request.getCaptchaToken())) {
            throw new BusinessServiceException("INVALID_CAPTCHA", "Güvenlik doğrulaması geçilemedi");
        }
        if (!request.isKvkkOnay()) {
            throw new ValidationServiceException("rezervasyon.kvkk.zorunlu", "KVKK aydınlatma metni onayı zorunludur");
        }
        validateGuestRequest(request);
        calendarService.assertAvailable(villa, request.getCheckIn(), request.getCheckOut());

        LocalDateTime holdExpiresAt = villa.getHoldExpiryHours() != null
                ? LocalDateTime.now().plusHours(villa.getHoldExpiryHours())
                : null;

        VillaReservation reservation = villaReservationRepository.save(VillaReservation.builder()
                .villa(villa)
                .guestName(request.getGuestName())
                .guestSurname(request.getGuestSurname())
                .guestEmail(request.getGuestEmail())
                .guestPhone(request.getGuestPhone())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .message(request.getMessage())
                .status(ReservationStatus.PENDING)
                .holdExpiresAt(holdExpiresAt)
                .kvkkOnay(true)
                .createdByAdmin(false)
                .build());

        log.info("Villa rezervasyon talebi alındı: id={}, villa={}, company={}", reservation.getId(), villa.getId(), company.getId());
        notifyCompanyAndGuest(company, villa, reservation);

        PublicVillaRezervasyonTalebiResponseDTO responseDTO = new PublicVillaRezervasyonTalebiResponseDTO();
        responseDTO.setMessages(List.of(AppMessageUtil.create(
                "rezervasyon.talebi.alindi", "Rezervasyon talebiniz alındı", AppMessageType.SUCCESS)));
        return responseDTO;
    }

    // ---------------------------------------------------------------------
    // Ortak yardımcılar
    // ---------------------------------------------------------------------

    private Company requireModuleEnabled(String apiKey) {
        Company company = companyRepository.findByApiKeyHashAndActiveTrue(apiKeyService.hash(apiKey))
                .orElseThrow(() -> new BusinessServiceException("INVALID_API_KEY", "Geçersiz API Key"));

        boolean enabled = rentSettingsRepository.findByCompany(company)
                .map(RentSettings::isEnabled)
                .orElse(false);
        if (!enabled) {
            throw new BusinessServiceException("rent.modul.kapali", "Bu firma için rezervasyon modülü aktif değildir");
        }
        return company;
    }

    private Villa findVillaOrThrow(Long villaId, Company company) {
        return villaRepository.findByIdAndCompanyAndDeletedFalseAndActiveTrue(villaId, company)
                .orElseThrow(() -> new ResourceNotFoundException("villa.bulunamadi", "Villa bulunamadı"));
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new ValidationServiceException("tarih.zorunlu", "Başlangıç ve bitiş tarihi zorunludur");
        }
        if (!end.isAfter(start)) {
            throw new ValidationServiceException("tarih.gecersiz", "Bitiş tarihi başlangıç tarihinden sonra olmalıdır");
        }
    }

    private void validateGuestRequest(PublicVillaRezervasyonTalebiRequestDTO request) {
        if (request.getGuestName() == null || request.getGuestName().isBlank()) {
            throw new ValidationServiceException("rezervasyon.misafir.zorunlu", "Misafir adı zorunludur");
        }
        if (request.getGuestEmail() == null || request.getGuestEmail().isBlank()) {
            throw new ValidationServiceException("rezervasyon.email.zorunlu", "Misafir e-postası zorunludur");
        }
        validateDateRange(request.getCheckIn(), request.getCheckOut());
        if (request.getCheckIn().isBefore(LocalDate.now())) {
            throw new ValidationServiceException("rezervasyon.tarih.gecmis", "Geçmiş bir tarih için rezervasyon oluşturulamaz");
        }
    }

    private void notifyCompanyAndGuest(Company company, Villa villa, VillaReservation reservation) {
        String recipient = (company.getContactEmail() != null && !company.getContactEmail().isBlank())
                ? company.getContactEmail()
                : company.getName();

        String companyHtml = String.format(
                "<html><body><h2>Yeni Rezervasyon Talebi</h2>" +
                        "<p>Villa: %s</p><p>Misafir: %s %s</p><p>Tarih: %s - %s</p><p>Telefon: %s</p><p>Not: %s</p></body></html>",
                villa.getName(), reservation.getGuestName(), reservation.getGuestSurname(),
                reservation.getCheckIn(), reservation.getCheckOut(), reservation.getGuestPhone(), reservation.getMessage());
        mailService.sendHtmlMail(recipient, recipient, "Yeni Villa Rezervasyon Talebi", companyHtml);

        String guestHtml = String.format(
                "<html><body><h2>Rezervasyon Talebiniz Alındı</h2>" +
                        "<p>%s villası için %s - %s tarihleri arasındaki rezervasyon talebiniz alınmıştır. " +
                        "Ödeme ve onay süreci için sizinle iletişime geçilecektir.</p></body></html>",
                villa.getName(), reservation.getCheckIn(), reservation.getCheckOut());
        mailService.sendHtmlMail(reservation.getGuestEmail(), reservation.getGuestName(), "Rezervasyon Talebiniz Alındı", guestHtml);

        companyContactInfoRepository.findByCompany(company).ifPresent(contactInfo -> {
            String text = String.format(
                    "*🏡 Yeni Rezervasyon Talebi*\n\n" +
                            "*Villa:* %s\n" +
                            "*Misafir:* %s %s\n" +
                            "*Tarih:* %s - %s\n" +
                            "*Telefon:* %s\n" +
                            "*Not:* %s",
                    villa.getName(), reservation.getGuestName(), nz(reservation.getGuestSurname()),
                    reservation.getCheckIn(), reservation.getCheckOut(),
                    nz(reservation.getGuestPhone()), nz(reservation.getMessage()));
            whatsAppService.sendMessage(contactInfo.getWhatsappNumber(), contactInfo.getWhatsappApiKey(), text);
        });
    }

    private static String nz(String value) {
        return (value == null || value.isBlank()) ? "-" : value;
    }

    private PublicVillaDTO convertToPublicVillaDTO(Villa villa) {
        List<PublicVillaImageDTO> images = villaImageRepository.findByVillaAndDeletedFalseOrderBySortOrderAscIdAsc(villa).stream()
                .map(img -> PublicVillaImageDTO.builder().id(img.getId()).fileName(img.getFileName()).fileType(img.getFileType()).build())
                .toList();

        boolean priceVisible = villa.isPriceVisible() && villa.getPrice() != null;

        return PublicVillaDTO.builder()
                .id(villa.getId())
                .name(villa.getName())
                .description(villa.getDescription())
                .capacity(villa.getCapacity())
                .price(priceVisible ? villa.getPrice() : null)
                .priceVisible(priceVisible)
                .images(images)
                .build();
    }
}
