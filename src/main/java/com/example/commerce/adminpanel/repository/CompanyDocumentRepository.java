package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyDocument;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, Long> {
    List<CompanyDocument> findByCompanyAndDeletedFalseOrderByUploadDateDesc(Company company);

    Optional<CompanyDocument> findByIdAndCompanyAndDeletedFalse(Long id, Company company);

    /** Belge içeriğini (bytea) hiç DB'den çekmeden yalnızca listeleme için metadata. */
    interface CompanyDocumentMetaView {
        Long getId();
        String getTitle();
        String getFileName();
        String getFileType();
        Long getFileSize();
        LocalDateTime getUploadDate();
    }

    @Query("SELECT d.id AS id, d.title AS title, d.fileName AS fileName, d.fileType AS fileType, d.fileSize AS fileSize, d.uploadDate AS uploadDate " +
            "FROM CompanyDocument d WHERE d.company = :company AND d.deleted = false ORDER BY d.uploadDate DESC")
    List<CompanyDocumentMetaView> findMetaByCompanyAndDeletedFalseOrderByUploadDateDesc(@Param("company") Company company);

    @Query("SELECT d.id AS id, d.title AS title, d.fileName AS fileName, d.fileType AS fileType, d.fileSize AS fileSize, d.uploadDate AS uploadDate " +
            "FROM CompanyDocument d WHERE d.id = :id AND d.company = :company AND d.deleted = false")
    Optional<CompanyDocumentMetaView> findMetaByIdAndCompanyAndDeletedFalse(@Param("id") Long id, @Param("company") Company company);
}
