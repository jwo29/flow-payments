package com.january.flowpayments.payment.repository;

import com.january.flowpayments.payment.domain.PaymentTransaction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {

    @Query("""
        SELECT pt
        FROM PaymentTransaction  pt
        WHERE pt.type = 'COMPENSATE_CANCEL'
          AND pt.status = 'FAIL'
          AND pt.retryCount < 5
    """)
    List<PaymentTransaction> findFailedCompensations(Pageable pageable);
}
