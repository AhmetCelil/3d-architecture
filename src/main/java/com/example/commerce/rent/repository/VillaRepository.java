package com.example.commerce.rent.repository;

import com.example.commerce.tenant.entity.Company;
import com.example.commerce.rent.entity.Villa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VillaRepository extends JpaRepository<Villa, Long> {
    List<Villa> findByCompanyAndDeletedFalseOrderByNameAsc(Company company);

    Optional<Villa> findByIdAndCompanyAndDeletedFalse(Long id, Company company);

    List<Villa> findByCompanyAndDeletedFalseAndActiveTrueOrderByNameAsc(Company company);

    Optional<Villa> findByIdAndCompanyAndDeletedFalseAndActiveTrue(Long id, Company company);
}
