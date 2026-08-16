package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyWhyUsItem;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyWhyUsItemRepository extends JpaRepository<CompanyWhyUsItem, Long> {
    List<CompanyWhyUsItem> findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(Company company);

    Optional<CompanyWhyUsItem> findByIdAndCompanyAndDeletedFalse(Long id, Company company);
}
