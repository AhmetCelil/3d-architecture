package com.example.commerce.rent.repository;

import com.example.commerce.rent.entity.Villa;
import com.example.commerce.rent.entity.VillaPriceOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VillaPriceOverrideRepository extends JpaRepository<VillaPriceOverride, Long> {
    List<VillaPriceOverride> findByVillaAndDeletedFalseOrderByStartDateAsc(Villa villa);

    Optional<VillaPriceOverride> findByIdAndVillaAndDeletedFalse(Long id, Villa villa);

    @Query("SELECT o FROM VillaPriceOverride o WHERE o.villa = :villa AND o.deleted = false " +
            "AND o.startDate < :end AND o.endDate > :start ORDER BY o.startDate ASC")
    List<VillaPriceOverride> findOverlapping(@Param("villa") Villa villa, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
