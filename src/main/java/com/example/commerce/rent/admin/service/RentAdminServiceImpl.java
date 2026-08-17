package com.example.commerce.rent.admin.service;

import com.example.commerce.auth.service.AuthenticationService;
import com.example.commerce.basedtos.AppMessageType;
import com.example.commerce.exception.BusinessServiceException;
import com.example.commerce.exception.ResourceNotFoundException;
import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.rent.admin.dto.*;
import com.example.commerce.rent.entity.*;
import com.example.commerce.rent.enums.ReservationStatus;
import com.example.commerce.rent.repository.*;
import com.example.commerce.rent.service.RentCalendarService;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.util.AppMessageUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentAdminServiceImpl implements RentAdminService {

    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 4000;
    private static final int MAX_NOTE_LENGTH = 500;
    private static final long MAX_IMAGE_SIZE_BYTES = 5L * 1024 * 1024;

    private final VillaRepository villaRepository;
    private final VillaImageRepository villaImageRepository;
    private final VillaPriceOverrideRepository villaPriceOverrideRepository;
    private final VillaAvailabilityBlockRepository villaAvailabilityBlockRepository;
    private final VillaReservationRepository villaReservationRepository;
    private final RentSettingsRepository rentSettingsRepository;
    private final RentCalendarService calendarService;
    private final AuthenticationService authenticationService;

    // ---------------------------------------------------------------------
    // Villa
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public VillalariListeleResponseDTO villalariListele() {
        Company company = requireModuleEnabled();
        List<Villa> villalar = villaRepository.findByCompanyAndDeletedFalseOrderByNameAsc(company);

        VillalariListeleResponseDTO responseDTO = new VillalariListeleResponseDTO();
        responseDTO.setData(villalar.stream().map(this::convertToVillaDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public RentIdResponseDTO villaEkle(VillaKaydetRequestDTO request) {
        Company company = requireModuleEnabled();
        validateVillaRequest(request);

        Villa villa = Villa.builder()
                .company(company)
                .name(request.getName())
                .description(request.getDescription())
                .capacity(request.getCapacity())
                .price(request.getPrice())
                .priceVisible(request.getPriceVisible() == null || request.getPriceVisible())
                .holdExpiryHours(request.getHoldExpiryHours())
                .active(request.getActive() == null || request.getActive())
                .build();

        Villa saved = villaRepository.save(villa);
        log.info("Villa eklendi: id={}, company={}", saved.getId(), company.getId());

        return idResponse(saved.getId(), "villa.ekleme.basarili", "Villa eklendi");
    }

    @Override
    @Transactional
    public RentAckResponseDTO villaGuncelle(Long id, VillaKaydetRequestDTO request) {
        Company company = requireModuleEnabled();
        validateVillaRequest(request);
        Villa villa = findVillaOrThrow(id, company);

        villa.setName(request.getName());
        villa.setDescription(request.getDescription());
        villa.setCapacity(request.getCapacity());
        villa.setPrice(request.getPrice());
        if (request.getPriceVisible() != null) {
            villa.setPriceVisible(request.getPriceVisible());
        }
        villa.setHoldExpiryHours(request.getHoldExpiryHours());
        if (request.getActive() != null) {
            villa.setActive(request.getActive());
        }

        villaRepository.save(villa);
        return ackResponse("villa.guncelleme.basarili", "Villa güncellendi");
    }

    @Override
    @Transactional
    public RentAckResponseDTO villaSil(Long id) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(id, company);
        villa.setDeleted(true);
        villaRepository.save(villa);
        return ackResponse("villa.silme.basarili", "Villa silindi");
    }

    // ---------------------------------------------------------------------
    // Galeri
    // ---------------------------------------------------------------------

    @Override
    @Transactional
    public RentAckResponseDTO gorselleriEkle(Long villaId, List<MultipartFile> images) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);

        if (images != null) {
            for (MultipartFile image : images) {
                if (image == null || image.isEmpty()) continue;
                if (image.getSize() > MAX_IMAGE_SIZE_BYTES) {
                    throw new ValidationServiceException("villa.gorsel.boyut",
                            "Villa görseli izin verilen " + (MAX_IMAGE_SIZE_BYTES / (1024 * 1024)) + "MB sınırını aşıyor");
                }
                try {
                    villaImageRepository.save(VillaImage.builder()
                            .villa(villa)
                            .fileData(image.getBytes())
                            .fileName(image.getOriginalFilename())
                            .fileType(image.getContentType())
                            .fileSize(image.getSize())
                            .uploadDate(LocalDateTime.now())
                            .sortOrder(0)
                            .build());
                } catch (IOException ex) {
                    log.error("Villa görseli işlenirken hata: {}", ex.getMessage(), ex);
                    throw new BusinessServiceException("villa.gorsel.hatasi", "Villa görseli yüklenirken hata oluştu");
                }
            }
        }

        return ackResponse("villa.gorsel.ekleme.basarili", "Görseller eklendi");
    }

    @Override
    @Transactional
    public RentAckResponseDTO gorselSil(Long villaId, Long imageId) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);
        VillaImage image = villaImageRepository.findByIdAndVillaAndDeletedFalse(imageId, villa)
                .orElseThrow(() -> new ResourceNotFoundException("villa.gorsel.bulunamadi", "Villa görseli bulunamadı"));

        image.setDeleted(true);
        villaImageRepository.save(image);
        return ackResponse("villa.gorsel.silme.basarili", "Görsel silindi");
    }

    @Override
    @Transactional(readOnly = true)
    public VillaGorsel gorselGetir(Long villaId, Long imageId) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);
        VillaImage image = villaImageRepository.findByIdAndVillaAndDeletedFalse(imageId, villa)
                .orElseThrow(() -> new ResourceNotFoundException("villa.gorsel.bulunamadi", "Villa görseli bulunamadı"));

        return new VillaGorsel(image.getFileName(), image.getFileType(), image.getFileData());
    }

    // ---------------------------------------------------------------------
    // Fiyat istisnaları
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public FiyatIstisnalariListeleResponseDTO fiyatIstisnalariniListele(Long villaId) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);

        FiyatIstisnalariListeleResponseDTO responseDTO = new FiyatIstisnalariListeleResponseDTO();
        responseDTO.setData(villaPriceOverrideRepository.findByVillaAndDeletedFalseOrderByStartDateAsc(villa).stream()
                .map(this::convertToFiyatIstisnasiDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public RentIdResponseDTO fiyatIstisnasiEkle(Long villaId, VillaFiyatIstisnasiKaydetRequestDTO request) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);
        validateDateRange(request.getStartDate(), request.getEndDate());
        if (request.getPrice() == null) {
            throw new ValidationServiceException("fiyat.istisnasi.fiyat.zorunlu", "Fiyat zorunludur");
        }
        validateTextLength(request.getNote(), MAX_NOTE_LENGTH, "Not");

        VillaPriceOverride override = villaPriceOverrideRepository.save(VillaPriceOverride.builder()
                .villa(villa)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .price(request.getPrice())
                .note(request.getNote())
                .build());

        return idResponse(override.getId(), "fiyat.istisnasi.ekleme.basarili", "Fiyat istisnası eklendi");
    }

    @Override
    @Transactional
    public RentAckResponseDTO fiyatIstisnasiSil(Long villaId, Long id) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);
        VillaPriceOverride override = villaPriceOverrideRepository.findByIdAndVillaAndDeletedFalse(id, villa)
                .orElseThrow(() -> new ResourceNotFoundException("fiyat.istisnasi.bulunamadi", "Fiyat istisnası bulunamadı"));

        override.setDeleted(true);
        villaPriceOverrideRepository.save(override);
        return ackResponse("fiyat.istisnasi.silme.basarili", "Fiyat istisnası silindi");
    }

    // ---------------------------------------------------------------------
    // Müsaitlik blokları
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public MusaitlikBloklariListeleResponseDTO musaitlikBloklariniListele(Long villaId) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);

        MusaitlikBloklariListeleResponseDTO responseDTO = new MusaitlikBloklariListeleResponseDTO();
        responseDTO.setData(villaAvailabilityBlockRepository.findByVillaAndDeletedFalseOrderByStartDateAsc(villa).stream()
                .map(this::convertToMusaitlikBloguDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public RentIdResponseDTO musaitlikBlokuEkle(Long villaId, MusaitlikBlokuKaydetRequestDTO request) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);
        validateDateRange(request.getStartDate(), request.getEndDate());
        validateTextLength(request.getNote(), MAX_NOTE_LENGTH, "Not");

        VillaAvailabilityBlock block = villaAvailabilityBlockRepository.save(VillaAvailabilityBlock.builder()
                .villa(villa)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .note(request.getNote())
                .build());

        return idResponse(block.getId(), "musaitlik.blogu.ekleme.basarili", "Müsaitlik bloğu eklendi");
    }

    @Override
    @Transactional
    public RentAckResponseDTO musaitlikBlokuSil(Long villaId, Long id) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);
        VillaAvailabilityBlock block = villaAvailabilityBlockRepository.findByIdAndVillaAndDeletedFalse(id, villa)
                .orElseThrow(() -> new ResourceNotFoundException("musaitlik.blogu.bulunamadi", "Müsaitlik bloğu bulunamadı"));

        block.setDeleted(true);
        villaAvailabilityBlockRepository.save(block);
        return ackResponse("musaitlik.blogu.silme.basarili", "Müsaitlik bloğu silindi");
    }

    // ---------------------------------------------------------------------
    // Rezervasyonlar
    // ---------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public RezervasyonlariListeleResponseDTO rezervasyonlariListele() {
        Company company = requireModuleEnabled();

        RezervasyonlariListeleResponseDTO responseDTO = new RezervasyonlariListeleResponseDTO();
        responseDTO.setData(villaReservationRepository.findByVilla_CompanyAndDeletedFalseOrderByCreatedAtDesc(company).stream()
                .map(this::convertToReservationDTO).toList());
        return responseDTO;
    }

    @Override
    @Transactional
    public RentIdResponseDTO rezervasyonEkle(Long villaId, RezervasyonKaydetRequestDTO request) {
        Company company = requireModuleEnabled();
        Villa villa = findVillaOrThrow(villaId, company);
        validateReservationRequest(request.getGuestName(), request.getGuestEmail(), request.getCheckIn(), request.getCheckOut());
        calendarService.assertAvailable(villa, request.getCheckIn(), request.getCheckOut());

        VillaReservation reservation = villaReservationRepository.save(VillaReservation.builder()
                .villa(villa)
                .guestName(request.getGuestName())
                .guestSurname(request.getGuestSurname())
                .guestEmail(request.getGuestEmail())
                .guestPhone(request.getGuestPhone())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .message(request.getMessage())
                .status(ReservationStatus.CONFIRMED)
                .kvkkOnay(true)
                .createdByAdmin(true)
                .build());

        log.info("Manuel rezervasyon eklendi: id={}, villa={}, company={}", reservation.getId(), villa.getId(), company.getId());
        return idResponse(reservation.getId(), "rezervasyon.ekleme.basarili", "Rezervasyon eklendi");
    }

    @Override
    @Transactional
    public RentAckResponseDTO rezervasyonDurumGuncelle(Long id, RezervasyonDurumGuncelleRequestDTO request) {
        Company company = requireModuleEnabled();
        VillaReservation reservation = villaReservationRepository.findByIdAndVilla_CompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("rezervasyon.bulunamadi", "Rezervasyon bulunamadı"));

        if (request.getStatus() == null) {
            throw new ValidationServiceException("rezervasyon.durum.zorunlu", "Durum zorunludur");
        }

        reservation.setStatus(request.getStatus());
        if (request.getStatus() != ReservationStatus.PENDING) {
            reservation.setHoldExpiresAt(null);
        }
        villaReservationRepository.save(reservation);

        return ackResponse("rezervasyon.durum.guncelleme.basarili", "Rezervasyon durumu güncellendi");
    }

    @Override
    @Transactional
    public RentAckResponseDTO rezervasyonHoldGuncelle(Long id, RezervasyonHoldGuncelleRequestDTO request) {
        Company company = requireModuleEnabled();
        VillaReservation reservation = villaReservationRepository.findByIdAndVilla_CompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("rezervasyon.bulunamadi", "Rezervasyon bulunamadı"));

        reservation.setHoldExpiresAt(request.getHoldExpiresAt());
        villaReservationRepository.save(reservation);

        return ackResponse("rezervasyon.hold.guncelleme.basarili", "Rezervasyon bekleme süresi güncellendi");
    }

    // ---------------------------------------------------------------------
    // Ortak yardımcılar
    // ---------------------------------------------------------------------

    private Company requireModuleEnabled() {
        Company company = authenticationService.getAuthenticatedUserCompany();
        boolean enabled = rentSettingsRepository.findByCompany(company)
                .map(RentSettings::isEnabled)
                .orElse(false);
        if (!enabled) {
            throw new BusinessServiceException("rent.modul.kapali", "Bu firma için rezervasyon modülü aktif değildir");
        }
        return company;
    }

    private Villa findVillaOrThrow(Long id, Company company) {
        return villaRepository.findByIdAndCompanyAndDeletedFalse(id, company)
                .orElseThrow(() -> new ResourceNotFoundException("villa.bulunamadi", "Villa bulunamadı"));
    }

    private void validateVillaRequest(VillaKaydetRequestDTO request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new ValidationServiceException("villa.ad.zorunlu", "Villa adı zorunludur");
        }
        validateTextLength(request.getName(), MAX_NAME_LENGTH, "Villa adı");
        validateTextLength(request.getDescription(), MAX_DESCRIPTION_LENGTH, "Açıklama");
        if (request.getHoldExpiryHours() != null && request.getHoldExpiryHours() <= 0) {
            throw new ValidationServiceException("villa.hold.gecersiz", "Bekleme süresi pozitif bir sayı olmalıdır");
        }
    }

    private void validateReservationRequest(String guestName, String guestEmail, java.time.LocalDate checkIn, java.time.LocalDate checkOut) {
        if (guestName == null || guestName.isBlank()) {
            throw new ValidationServiceException("rezervasyon.misafir.zorunlu", "Misafir adı zorunludur");
        }
        if (guestEmail == null || guestEmail.isBlank()) {
            throw new ValidationServiceException("rezervasyon.email.zorunlu", "Misafir e-postası zorunludur");
        }
        validateDateRange(checkIn, checkOut);
    }

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start == null || end == null) {
            throw new ValidationServiceException("tarih.zorunlu", "Başlangıç ve bitiş tarihi zorunludur");
        }
        if (!end.isAfter(start)) {
            throw new ValidationServiceException("tarih.gecersiz", "Bitiş tarihi başlangıç tarihinden sonra olmalıdır");
        }
    }

    private void validateTextLength(String value, int maxLength, String fieldLabel) {
        if (value != null && value.length() > maxLength) {
            throw new ValidationServiceException("rent.metin.uzunluk", fieldLabel + " en fazla " + maxLength + " karakter olabilir");
        }
    }

    private RentAckResponseDTO ackResponse(String code, String text) {
        RentAckResponseDTO responseDTO = new RentAckResponseDTO();
        responseDTO.setMessages(List.of(AppMessageUtil.create(code, text, AppMessageType.SUCCESS)));
        return responseDTO;
    }

    private RentIdResponseDTO idResponse(Long id, String code, String text) {
        RentIdResponseDTO responseDTO = new RentIdResponseDTO();
        responseDTO.setId(id);
        responseDTO.setMessages(List.of(AppMessageUtil.create(code, text, AppMessageType.SUCCESS)));
        return responseDTO;
    }

    private VillaDTO convertToVillaDTO(Villa villa) {
        List<VillaImageDTO> images = villaImageRepository.findByVillaAndDeletedFalseOrderBySortOrderAscIdAsc(villa).stream()
                .map(img -> VillaImageDTO.builder()
                        .id(img.getId())
                        .fileName(img.getFileName())
                        .fileType(img.getFileType())
                        .fileSize(img.getFileSize())
                        .sortOrder(img.getSortOrder())
                        .build())
                .toList();

        return VillaDTO.builder()
                .id(villa.getId())
                .name(villa.getName())
                .description(villa.getDescription())
                .capacity(villa.getCapacity())
                .price(villa.getPrice())
                .priceVisible(villa.isPriceVisible())
                .holdExpiryHours(villa.getHoldExpiryHours())
                .active(villa.isActive())
                .images(images)
                .build();
    }

    private VillaFiyatIstisnasiDTO convertToFiyatIstisnasiDTO(VillaPriceOverride override) {
        return VillaFiyatIstisnasiDTO.builder()
                .id(override.getId())
                .startDate(override.getStartDate())
                .endDate(override.getEndDate())
                .price(override.getPrice())
                .note(override.getNote())
                .build();
    }

    private MusaitlikBloguDTO convertToMusaitlikBloguDTO(VillaAvailabilityBlock block) {
        return MusaitlikBloguDTO.builder()
                .id(block.getId())
                .startDate(block.getStartDate())
                .endDate(block.getEndDate())
                .note(block.getNote())
                .build();
    }

    private VillaReservationDTO convertToReservationDTO(VillaReservation reservation) {
        return VillaReservationDTO.builder()
                .id(reservation.getId())
                .villaId(reservation.getVilla().getId())
                .villaName(reservation.getVilla().getName())
                .guestName(reservation.getGuestName())
                .guestSurname(reservation.getGuestSurname())
                .guestEmail(reservation.getGuestEmail())
                .guestPhone(reservation.getGuestPhone())
                .checkIn(reservation.getCheckIn())
                .checkOut(reservation.getCheckOut())
                .status(reservation.getStatus())
                .holdExpiresAt(reservation.getHoldExpiresAt())
                .message(reservation.getMessage())
                .createdByAdmin(reservation.isCreatedByAdmin())
                .createdAt(reservation.getCreatedAt())
                .build();
    }
}
