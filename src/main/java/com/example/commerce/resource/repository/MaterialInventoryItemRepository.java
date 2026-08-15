package com.example.commerce.resource.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.resource.entity.MaterialInventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialInventoryItemRepository extends JpaRepository<MaterialInventoryItem, Long> {
    List<MaterialInventoryItem> findByProject(CompanyProject project);
}
