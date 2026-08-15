package com.example.commerce.resource.repository;

import com.example.commerce.resource.entity.Personnel;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonnelRepository extends JpaRepository<Personnel, Long> {
    List<Personnel> findByCompanyAndActiveTrue(Company company);

    Optional<Personnel> findByIdAndCompany(Long id, Company company);
}
