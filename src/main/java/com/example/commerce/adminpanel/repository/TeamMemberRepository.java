package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.TeamMember;
import com.example.commerce.tenant.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    List<TeamMember> findByCompanyAndDeletedFalseOrderByDisplayOrderAsc(Company company);

    List<TeamMember> findByCompanyAndDeletedFalseAndActiveTrueOrderByDisplayOrderAsc(Company company);

    Optional<TeamMember> findByIdAndCompanyAndDeletedFalse(Long id, Company company);
}
