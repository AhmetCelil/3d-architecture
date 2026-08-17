package com.example.commerce.rent.service;

import com.example.commerce.rent.entity.VillaReservation;
import com.example.commerce.rent.enums.ReservationStatus;
import com.example.commerce.rent.repository.VillaReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/** Bekleyen rezervasyonların, admin'in belirlediği süre geçtiğinde otomatik serbest bırakılmasını sağlar. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RentReservationExpiryScheduler {

    private static final long CHECK_INTERVAL_MS = 15 * 60 * 1000;

    private final VillaReservationRepository villaReservationRepository;

    @Scheduled(fixedRate = CHECK_INTERVAL_MS)
    @Transactional
    public void expireStaleHolds() {
        List<VillaReservation> expired = villaReservationRepository
                .findByStatusAndHoldExpiresAtBefore(ReservationStatus.PENDING, LocalDateTime.now());

        if (expired.isEmpty()) return;

        expired.forEach(r -> r.setStatus(ReservationStatus.EXPIRED));
        villaReservationRepository.saveAll(expired);
        log.info("{} adet bekleyen villa rezervasyonu süresi doldu, serbest bırakıldı", expired.size());
    }
}
