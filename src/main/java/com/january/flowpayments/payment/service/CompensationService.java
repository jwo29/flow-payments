package com.january.flowpayments.payment.service;

import com.january.flowpayments.payment.domain.Payment;
import com.january.flowpayments.payment.domain.PaymentTransaction;
import com.january.flowpayments.payment.dto.PaymentRefundRequestDTO;
import com.january.flowpayments.payment.processor.PaymentProcessor;
import com.january.flowpayments.payment.processor.PaymentProcessorFactory;
import com.january.flowpayments.payment.repository.PaymentRepository;
import com.january.flowpayments.payment.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CompensationService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository txRepository;
    private final PaymentProcessorFactory factory;

    @Transactional
    public void retryFailedCompensation() {
        List<PaymentTransaction> failedList = txRepository.findFailedCompensations();

        for (PaymentTransaction tx : failedList) {

            Payment payment = paymentRepository.findByPaymentId(tx.getPaymentId());

            PaymentProcessor processor = factory.get(payment.getPaymentMethod());

            try {
                processor.refund(payment, new PaymentRefundRequestDTO(
                        payment.getPaymentId(),
                        payment.getAmount(),
                        null
                ));

                payment.cancel();

                tx.markSuccess();
            } catch (Exception e) {
                // 재시도 횟수 + 1
                tx.increaseRetryCount();
            }
        }
    }
}
