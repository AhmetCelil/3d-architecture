package com.example.commerce.rent.repository;

import com.example.commerce.rent.entity.Villa;
import com.example.commerce.rent.entity.VillaImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VillaImageRepository extends JpaRepository<VillaImage, Long> {
    List<VillaImage> findByVillaAndDeletedFalseOrderBySortOrderAscIdAsc(Villa villa);

    Optional<VillaImage> findByIdAndVillaAndDeletedFalse(Long id, Villa villa);
}
