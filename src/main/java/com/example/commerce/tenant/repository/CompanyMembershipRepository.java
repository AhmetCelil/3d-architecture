package com.example.commerce.tenant.repository;

import com.example.commerce.auth.entity.User;
import com.example.commerce.tenant.entity.Company;
import com.example.commerce.tenant.entity.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {
    List<CompanyMembership> findByUser(User user);

    Optional<CompanyMembership> findByUserAndCompany(User user, Company company);

    Optional<CompanyMembership> findFirstByUserAndDeletedFalseOrderByIdAsc(User user);
}
