package com.january.ledgerflow.payment.repository;

import com.january.ledgerflow.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByPaymentId(Long paymentId);
    Optional<Payment> findByOrderId(String orderId);
}
