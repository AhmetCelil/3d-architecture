package com.example.commerce.schedule.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.schedule.entity.SiteDiaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteDiaryEntryRepository extends JpaRepository<SiteDiaryEntry, Long> {
    List<SiteDiaryEntry> findByProjectOrderByReportDateDesc(CompanyProject project);
}
