package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.Announcement;
import com.example.commerce.adminpanel.enums.AnnouncementType;
import com.example.commerce.adminpanel.enums.DisplayFrequency;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByCompanyAndDeletedFalseOrderByPriorityDescCreatedAtDesc(Company company);

    Optional<Announcement> findByIdAndCompanyAndDeletedFalse(Long id, Company company);

    /** Görsel içeriğini (bytea) hiç DB'den çekmeden yalnızca listeleme için metadata (hasImage, imageFileName != null ile hesaplanır). */
    interface AnnouncementMetaView {
        Long getId();
        String getTitle();
        String getMessage();
        AnnouncementType getAnnouncementType();
        String getImageFileName();
        String getLinkUrl();
        String getButtonText();
        Integer getPriority();
        DisplayFrequency getDisplayFrequency();
        LocalDateTime getStartDate();
        LocalDateTime getEndDate();
        boolean isActive();
        LocalDateTime getCreatedAt();
    }

    @Query("SELECT a.id AS id, a.title AS title, a.message AS message, a.announcementType AS announcementType, " +
            "a.imageFileName AS imageFileName, a.linkUrl AS linkUrl, a.buttonText AS buttonText, a.priority AS priority, " +
            "a.displayFrequency AS displayFrequency, a.startDate AS startDate, a.endDate AS endDate, a.active AS active, " +
            "a.createdAt AS createdAt " +
            "FROM Announcement a WHERE a.company = :company AND a.deleted = false ORDER BY a.priority DESC, a.createdAt DESC")
    List<AnnouncementMetaView> findMetaByCompanyAndDeletedFalseOrderByPriorityDescCreatedAtDesc(@Param("company") Company company);

    interface AnnouncementImageMetaView {
        String getImageFileName();
        String getImageFileType();
    }

    @Query("SELECT a.imageFileName AS imageFileName, a.imageFileType AS imageFileType " +
            "FROM Announcement a WHERE a.id = :id AND a.company = :company AND a.deleted = false")
    Optional<AnnouncementImageMetaView> findImageMetaByIdAndCompanyAndDeletedFalse(@Param("id") Long id, @Param("company") Company company);
}
