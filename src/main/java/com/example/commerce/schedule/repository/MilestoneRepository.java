package com.example.commerce.schedule.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.schedule.entity.Milestone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MilestoneRepository extends JpaRepository<Milestone, Long> {
    List<Milestone> findByProject(CompanyProject project);
}
