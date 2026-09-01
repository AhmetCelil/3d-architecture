package com.example.commerce.auth.repository;

import com.example.commerce.auth.entity.User;
import com.example.commerce.auth.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByEmailAndDeletedFalse(String email);

    /** Süper admin panelinde kullanıcıları opsiyonel şirket/rol filtresiyle sayfalı listeler. */
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN u.companyMemberships cm " +
            "WHERE u.deleted = false " +
            "AND (:companyId IS NULL OR cm.company.id = :companyId) " +
            "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchForSuperAdmin(@Param("companyId") Long companyId, @Param("role") Role role, Pageable pageable);
}
