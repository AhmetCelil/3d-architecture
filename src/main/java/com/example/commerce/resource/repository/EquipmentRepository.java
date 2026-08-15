package com.example.commerce.resource.repository;

import com.example.commerce.resource.entity.Equipment;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    List<Equipment> findByCompany(Company company);

    Optional<Equipment> findByIdAndCompany(Long id, Company company);
}
