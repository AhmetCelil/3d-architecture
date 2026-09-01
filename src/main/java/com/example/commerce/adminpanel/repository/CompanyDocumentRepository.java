package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyDocument;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyDocumentRepository extends JpaRepository<CompanyDocument, Long> {
    List<CompanyDocument> findByCompanyAndDeletedFalseOrderByUploadDateDesc(Company company);

    Optional<CompanyDocument> findByIdAndCompanyAndDeletedFalse(Long id, Company company);
}
