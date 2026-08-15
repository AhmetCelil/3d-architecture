package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.ProjectFile;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProjectFileRepository extends JpaRepository<ProjectFile, Long> {
    Optional<ProjectFile> findByIdAndDeletedFalseAndProject_CompanyAndProject_DeletedFalse(Long id, Company company);
}

