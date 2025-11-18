package com.example.commerce.adminpanel.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyProjectRepository extends JpaRepository<CompanyProject, Long> {
    List<CompanyProject> findByUserAndDeletedFalse(User user);

    Optional<CompanyProject> findByIdAndDeletedFalse(Long id);

    Optional<CompanyProject> findByIdAndUserAndDeletedFalse(Long id, User user);

    Optional<CompanyProject> findByUniqueCodeAndDeletedFalse(String uniqueCode);

    boolean existsByUniqueCode(String uniqueCode);

}