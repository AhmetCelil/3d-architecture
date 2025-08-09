package com.example.commerce.profilayarlama.repository;


import com.example.commerce.profilayarlama.entity.CompanyProject;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    // Örneğin: List<CompanyProject> findByUserId(Long userId);
}
