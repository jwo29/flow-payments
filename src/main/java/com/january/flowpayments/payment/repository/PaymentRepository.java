package com.january.flowpayments.payment.repository;

import com.january.flowpayments.payment.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Payment findByPaymentId(Long paymentId);
    Optional<Payment> findByOrderId(String orderId);
}
