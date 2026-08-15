package com.example.commerce.resource.repository;

import com.example.commerce.resource.entity.Subcontractor;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SubcontractorRepository extends JpaRepository<Subcontractor, Long> {
    List<Subcontractor> findByCompany(Company company);

    Optional<Subcontractor> findByIdAndCompany(Long id, Company company);
}
