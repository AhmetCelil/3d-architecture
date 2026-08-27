package com.example.commerce.rent.service;

import com.example.commerce.exception.ValidationServiceException;
import com.example.commerce.rent.entity.Villa;
import com.example.commerce.rent.entity.VillaPriceOverride;
import com.example.commerce.rent.repository.VillaAvailabilityBlockRepository;
import com.example.commerce.rent.repository.VillaPriceOverrideRepository;
import com.example.commerce.rent.repository.VillaRepository;
import com.example.commerce.rent.repository.VillaReservationRepository;
import com.example.commerce.tenant.entity.Company;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Müsaitlik ve fiyat hesabı hem admin hem public taraf için ortak: bir tarih
 * aralığının dolu sayılması için rezervasyon ve manuel blok kaynaklarının
 * ikisi de kontrol edilir; fiyat için önce istisna, yoksa villa'nın sabit fiyatı kullanılır.
 */
@Service
@RequiredArgsConstructor
public class RentCalendarService {

    private final VillaReservationRepository villaReservationRepository;
    private final VillaAvailabilityBlockRepository villaAvailabilityBlockRepository;
    private final VillaPriceOverrideRepository villaPriceOverrideRepository;
    private final VillaRepository villaRepository;

    public boolean isAvailable(Villa villa, LocalDate checkIn, LocalDate checkOut) {
        return villaReservationRepository.findActiveOverlapping(villa, checkIn, checkOut).isEmpty()
                && villaAvailabilityBlockRepository.findOverlapping(villa, checkIn, checkOut).isEmpty();
    }

    public void assertAvailable(Villa villa, LocalDate checkIn, LocalDate checkOut) {
        if (!isAvailable(villa, checkIn, checkOut)) {
            throw new ValidationServiceException("rent.tarih.musait-degil", "Seçilen tarihler için villa müsait değildir");
        }
    }

    /** [start, end) aralığında aktif olan (silinmemiş, active) villalar arasından, o aralıkta tamamen müsait olanları döner. */
    public List<Villa> availableVillas(Company company, LocalDate start, LocalDate end) {
        Set<Long> unavailableVillaIds = new HashSet<>(villaReservationRepository.findVillaIdsWithActiveOverlap(company, start, end));
        unavailableVillaIds.addAll(villaAvailabilityBlockRepository.findVillaIdsWithOverlap(company, start, end));

        return villaRepository.findByCompanyAndDeletedFalseAndActiveTrueOrderByNameAsc(company).stream()
                .filter(villa -> !unavailableVillaIds.contains(villa.getId()))
                .toList();
    }

    /** [start, end) aralığında dolu olan tarih aralıklarını (rezervasyon + manuel blok birleşimi) döner. */
    public List<DateRange> blockedRanges(Villa villa, LocalDate start, LocalDate end) {
        List<DateRange> ranges = new ArrayList<>();
        villaReservationRepository.findActiveOverlapping(villa, start, end)
                .forEach(r -> ranges.add(new DateRange(r.getCheckIn(), r.getCheckOut())));
        villaAvailabilityBlockRepository.findOverlapping(villa, start, end)
                .forEach(b -> ranges.add(new DateRange(b.getStartDate(), b.getEndDate())));
        return ranges;
    }

    public BigDecimal effectivePrice(Villa villa, LocalDate date) {
        return dailyPrice(villa, date).price();
    }

    /** O gün için geçerli fiyatı döner; bir fiyat istisnası varsa onu (special=true), yoksa villa'nın genel fiyatını kullanır. */
    public DailyPrice dailyPrice(Villa villa, LocalDate date) {
        return villaPriceOverrideRepository.findOverlapping(villa, date, date.plusDays(1)).stream()
                .findFirst()
                .map(o -> new DailyPrice(date, o.getPrice(), true))
                .orElseGet(() -> new DailyPrice(date, villa.getPrice(), false));
    }

    /** [start, end) aralığındaki her gün için fiyat takvimini tek seferde (N+1 sorgu olmadan) döner. */
    public List<DailyPrice> dailyPrices(Villa villa, LocalDate start, LocalDate end) {
        List<VillaPriceOverride> overrides = villaPriceOverrideRepository.findOverlapping(villa, start, end);
        return start.datesUntil(end)
                .map(date -> overrides.stream()
                        .filter(o -> !date.isBefore(o.getStartDate()) && date.isBefore(o.getEndDate()))
                        .findFirst()
                        .map(o -> new DailyPrice(date, o.getPrice(), true))
                        .orElseGet(() -> new DailyPrice(date, villa.getPrice(), false)))
                .toList();
    }

    public record DateRange(LocalDate startDate, LocalDate endDate) {}

    public record DailyPrice(LocalDate date, BigDecimal price, boolean special) {}
}
