package com.example.statemachinedemo.repository;

import com.example.statemachinedemo.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {

}
