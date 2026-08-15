package com.example.commerce.contract.repository;

import com.example.commerce.contract.entity.Contract;
import com.example.commerce.contract.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByContract(Contract contract);
}
