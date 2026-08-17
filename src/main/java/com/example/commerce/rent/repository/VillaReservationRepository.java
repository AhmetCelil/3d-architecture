package com.example.commerce.rent.repository;

import com.example.commerce.tenant.entity.Company;
import com.example.commerce.rent.entity.Villa;
import com.example.commerce.rent.entity.VillaReservation;
import com.example.commerce.rent.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface VillaReservationRepository extends JpaRepository<VillaReservation, Long> {
    List<VillaReservation> findByVilla_CompanyAndDeletedFalseOrderByCreatedAtDesc(Company company);

    List<VillaReservation> findByVillaAndDeletedFalseOrderByCheckInDesc(Villa villa);

    Optional<VillaReservation> findByIdAndVilla_CompanyAndDeletedFalse(Long id, Company company);

    /** O villada, tarih aralığını dolduran aktif kayıtlar: onaylanmış rezervasyonlar + hâlâ kilidi geçerli bekleyen talepler. */
    @Query("SELECT r FROM VillaReservation r WHERE r.villa = :villa AND r.deleted = false " +
            "AND (r.status = com.example.commerce.rent.enums.ReservationStatus.CONFIRMED " +
            "     OR (r.status = com.example.commerce.rent.enums.ReservationStatus.PENDING " +
            "         AND (r.holdExpiresAt IS NULL OR r.holdExpiresAt > CURRENT_TIMESTAMP))) " +
            "AND r.checkIn < :end AND r.checkOut > :start")
    List<VillaReservation> findActiveOverlapping(@Param("villa") Villa villa, @Param("start") LocalDate start, @Param("end") LocalDate end);

    List<VillaReservation> findByStatusAndHoldExpiresAtBefore(ReservationStatus status, LocalDateTime moment);
}
