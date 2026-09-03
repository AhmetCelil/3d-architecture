package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.adminpanel.entity.UnitType;
import com.example.commerce.adminpanel.enums.FileCategory;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    Optional<ProjectFile> findByIdAndDeletedFalseAndProject_CompanyAndProject_DeletedFalse(Long id, Company company);

    Optional<ProjectFile> findByIdAndProject_IdAndDeletedFalse(Long id, Long projectId);

    long countByProjectAndDeletedFalse(CompanyProject project);

    long countByUnitTypeAndDeletedFalse(UnitType unitType);

    /** Dosya içeriğini (bytea) hiç çekmeden yalnızca listeleme/detay ekranları için metadata. */
    interface ProjectFileMetaView {
        Long getId();
        String getFileName();
        String getFileType();
        Long getFileSize();
        String getTitle();
        FileCategory getFileCategory();
        LocalDateTime getUploadDate();
    }

    @Query("SELECT f.id AS id, f.fileName AS fileName, f.fileType AS fileType, f.fileSize AS fileSize, " +
            "f.title AS title, f.fileCategory AS fileCategory, f.uploadDate AS uploadDate " +
            "FROM ProjectFile f WHERE f.project = :project AND f.deleted = false")
    List<ProjectFileMetaView> findMetaByProjectAndDeletedFalse(@Param("project") CompanyProject project);

    @Query("SELECT f.id AS id, f.fileName AS fileName, f.fileType AS fileType, f.fileSize AS fileSize, " +
            "f.title AS title, f.fileCategory AS fileCategory, f.uploadDate AS uploadDate " +
            "FROM ProjectFile f WHERE f.unitType = :unitType AND f.deleted = false")
    List<ProjectFileMetaView> findMetaByUnitTypeAndDeletedFalse(@Param("unitType") UnitType unitType);

    @Query("SELECT f.id AS id, f.fileName AS fileName, f.fileType AS fileType, f.fileSize AS fileSize, " +
            "f.title AS title, f.fileCategory AS fileCategory, f.uploadDate AS uploadDate " +
            "FROM ProjectFile f WHERE f.id = :id AND f.project.id = :projectId AND f.deleted = false")
    Optional<ProjectFileMetaView> findMetaByIdAndProjectId(@Param("id") Long id, @Param("projectId") Long projectId);

    interface RoomDetailRow {
        Long getFileId();
        String getRoomName();
        String getValue();
    }

    @Query("SELECT f.id AS fileId, rd.roomName AS roomName, rd.value AS value " +
            "FROM ProjectFile f JOIN f.roomDetails rd WHERE f.id IN :fileIds")
    List<RoomDetailRow> findRoomDetailRows(@Param("fileIds") List<Long> fileIds);
}

