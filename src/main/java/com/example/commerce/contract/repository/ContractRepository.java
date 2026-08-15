package com.example.commerce.contract.repository;

import com.example.commerce.adminpanel.entity.CompanyProject;
import com.example.commerce.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {
    List<Contract> findByProject(CompanyProject project);

    Optional<Contract> findByIdAndProject(Long id, CompanyProject project);
}
