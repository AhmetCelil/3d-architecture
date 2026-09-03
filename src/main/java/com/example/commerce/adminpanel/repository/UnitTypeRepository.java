package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.adminpanel.entity.UnitType;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnitTypeRepository extends JpaRepository<UnitType, Long> {
    List<UnitType> findByProjectAndDeletedFalse(CompanyProject project);

    Optional<UnitType> findByIdAndProject_CompanyAndDeletedFalse(Long id, Company company);
}
