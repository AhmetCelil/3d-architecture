package com.example.commerce.resource.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.resource.entity.SubcontractorAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubcontractorAssignmentRepository extends JpaRepository<SubcontractorAssignment, Long> {
    List<SubcontractorAssignment> findByProject(CompanyProject project);
}
