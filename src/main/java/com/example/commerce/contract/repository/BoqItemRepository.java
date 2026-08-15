package com.example.commerce.contract.repository;

import com.example.commerce.contract.entity.BoqItem;
import com.example.commerce.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoqItemRepository extends JpaRepository<BoqItem, Long> {
    List<BoqItem> findByContract(Contract contract);
}
