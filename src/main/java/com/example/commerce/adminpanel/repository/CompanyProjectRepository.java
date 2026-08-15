package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    List<CompanyProject> findByCompanyAndDeletedFalse(Company company);

    Page<CompanyProject> findByCompanyAndDeletedFalse(Company company, Pageable pageable);

    Optional<CompanyProject> findByIdAndDeletedFalse(Long id);

    Optional<CompanyProject> findByIdAndCompanyAndDeletedFalse(Long id, Company company);

    Optional<CompanyProject> findByUniqueCodeAndDeletedFalse(String uniqueCode);

    boolean existsByUniqueCode(String uniqueCode);

}