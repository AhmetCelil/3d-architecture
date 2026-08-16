package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.ContactMessage;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
    Page<ContactMessage> findByCompanyAndDeletedFalseOrderByCreatedAtDesc(Company company, Pageable pageable);

    Page<ContactMessage> findByCompanyAndDeletedFalseAndReadOrderByCreatedAtDesc(Company company, boolean read, Pageable pageable);

    Optional<ContactMessage> findByIdAndCompanyAndDeletedFalse(Long id, Company company);
}
