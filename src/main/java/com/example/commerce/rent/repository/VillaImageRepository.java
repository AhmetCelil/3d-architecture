package com.example.commerce.rent.repository;

import com.example.commerce.rent.entity.Villa;
import com.example.commerce.rent.entity.VillaImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VillaImageRepository extends JpaRepository<VillaImage, Long> {
    List<VillaImage> findByVillaAndDeletedFalseOrderBySortOrderAscIdAsc(Villa villa);

    Optional<VillaImage> findByIdAndVillaAndDeletedFalse(Long id, Villa villa);

    /** Görsel içeriğini (bytea) hiç DB'den çekmeden yalnızca galeri listesi için metadata. */
    interface VillaImageMetaView {
        Long getId();
        String getFileName();
        String getFileType();
        Long getFileSize();
        Integer getSortOrder();
    }

    @Query("SELECT i.id AS id, i.fileName AS fileName, i.fileType AS fileType, i.fileSize AS fileSize, i.sortOrder AS sortOrder " +
            "FROM VillaImage i WHERE i.villa = :villa AND i.deleted = false ORDER BY i.sortOrder ASC, i.id ASC")
    List<VillaImageMetaView> findMetaByVillaAndDeletedFalseOrderBySortOrderAscIdAsc(@Param("villa") Villa villa);

    interface VillaImageFileMetaView {
        String getFileName();
        String getFileType();
    }

    @Query("SELECT i.fileName AS fileName, i.fileType AS fileType FROM VillaImage i WHERE i.id = :id AND i.villa = :villa AND i.deleted = false")
    Optional<VillaImageFileMetaView> findFileMetaByIdAndVillaAndDeletedFalse(@Param("id") Long id, @Param("villa") Villa villa);
}
