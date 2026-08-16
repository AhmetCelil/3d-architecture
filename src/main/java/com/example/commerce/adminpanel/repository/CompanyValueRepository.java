package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyValue;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyValueRepository extends JpaRepository<CompanyValue, Long> {
    List<CompanyValue> findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(Company company);

    Optional<CompanyValue> findByIdAndCompanyAndDeletedFalse(Long id, Company company);
}
