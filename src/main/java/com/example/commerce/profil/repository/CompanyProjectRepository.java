package com.example.commerce.profil.repository;


import com.example.commerce.profil.entity.CompanyProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    // Örneğin: List<CompanyProject> findByUserId(Long userId);
}
