package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.Announcement;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByCompanyAndDeletedFalseOrderByPriorityDescCreatedAtDesc(Company company);

    Optional<Announcement> findByIdAndCompanyAndDeletedFalse(Long id, Company company);
}
