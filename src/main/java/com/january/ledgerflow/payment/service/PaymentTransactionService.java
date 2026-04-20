package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.PaymentApproveRequestDTO;
import com.january.ledgerflow.payment.exception.PaymentException;
import com.january.ledgerflow.payment.repository.PaymentRepository;
import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import com.january.ledgerflow.transaction.dto.WithdrawRequestDTO;
import com.january.ledgerflow.transaction.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;

    public Payment getOrCreatePayment(PaymentApproveRequestDTO paymentApproveRequestDTO) {
        return paymentRepository.findByOrderId(paymentApproveRequestDTO.getOrderId())
                .orElseGet(() -> paymentRepository.save(
                        new Payment(
                                paymentApproveRequestDTO.getMerchantId(),
                                paymentApproveRequestDTO.getUserId(),
                                paymentApproveRequestDTO.getAccountId(),
                                paymentApproveRequestDTO.getOrderId(),
                                paymentApproveRequestDTO.getAmount(),
                                paymentApproveRequestDTO.getPaymentMethod()
                        )
                ));
    }

    @Transactional
    public void completePayment(Long paymentId, PgApproveResponseDTO pgApproveResponseDTO) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (!"APPROVED".equals(pgApproveResponseDTO.getStatus())) {
            payment.fail(pgApproveResponseDTO.getMessage());

            throw new PaymentException("PG 승인 실패: " + pgApproveResponseDTO.getMessage());
        }

        payment.approve(pgApproveResponseDTO.getPgTransactionId(), pgApproveResponseDTO.getAuthCode());

        // 5. 계좌 잔액 차감
        transactionService.withdraw(
                new WithdrawRequestDTO(
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );

    }

    @Transactional
    public Payment getRefundablePayment(Long paymentId, BigDecimal amount) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (!payment.getStatus().canRefund()) {
            throw new IllegalStateException("환불 불가 상태");
        }

        if (payment.getRemainingAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("환불 금액 초과");
        }

        return payment;
    }

}
