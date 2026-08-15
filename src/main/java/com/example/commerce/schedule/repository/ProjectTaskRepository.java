package com.example.commerce.schedule.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.schedule.entity.ProjectTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectTaskRepository extends JpaRepository<ProjectTask, Long> {
    List<ProjectTask> findByProject(CompanyProject project);
}
