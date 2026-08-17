package com.example.commerce.rent.repository;

import com.example.commerce.rent.entity.Villa;
import com.example.commerce.rent.entity.VillaAvailabilityBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VillaAvailabilityBlockRepository extends JpaRepository<VillaAvailabilityBlock, Long> {
    List<VillaAvailabilityBlock> findByVillaAndDeletedFalseOrderByStartDateAsc(Villa villa);

    Optional<VillaAvailabilityBlock> findByIdAndVillaAndDeletedFalse(Long id, Villa villa);

    @Query("SELECT b FROM VillaAvailabilityBlock b WHERE b.villa = :villa AND b.deleted = false " +
            "AND b.startDate < :end AND b.endDate > :start")
    List<VillaAvailabilityBlock> findOverlapping(@Param("villa") Villa villa, @Param("start") LocalDate start, @Param("end") LocalDate end);
}
