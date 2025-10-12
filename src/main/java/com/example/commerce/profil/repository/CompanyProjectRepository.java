package com.example.commerce.profil.repository;

import com.example.commerce.profil.entity.CompanyProject;
import com.example.commerce.profil.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {

    List<CompanyProject> findByUserId(Long userId);

    List<CompanyProject> findByUserIdAndStatus(Long userId, ProjectStatus status);

    List<CompanyProject> findByUserIdOrderByStartDateDesc(Long userId);
}