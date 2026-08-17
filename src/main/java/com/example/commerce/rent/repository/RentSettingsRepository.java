package com.example.commerce.rent.repository;

import com.example.commerce.rent.entity.RentSettings;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RentSettingsRepository extends JpaRepository<RentSettings, Long> {
    Optional<RentSettings> findByCompany(Company company);
}
