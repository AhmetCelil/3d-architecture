package com.example.commerce.tenant.repository;

import com.example.commerce.tenant.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByApiKeyHashAndActiveTrue(String apiKeyHash);

    Page<Company> findByNameContainingIgnoreCase(String search, Pageable pageable);
}
