package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.exception.PaymentException;
import com.january.ledgerflow.payment.repository.PaymentRepository;
import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import com.january.ledgerflow.pg.dto.PgCancelResponseDTO;
import com.january.ledgerflow.transaction.dto.DepositRequestDTO;
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

    @Transactional
    public void completePayment(Long paymentId, PgApproveResponseDTO pgApproveResponseDTO) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (!"APPROVED".equals(pgApproveResponseDTO.getStatus())) {
            payment.fail(pgApproveResponseDTO.getMessage());

            throw new PaymentException("PG 승인 실패: " + pgApproveResponseDTO.getMessage());
        }

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

        if (!payment.canRefund()) {
            throw new IllegalStateException("환불 불가 상태");
        }

        if (payment.getRemainingAmount().compareTo(amount) < 0) {
            throw new IllegalArgumentException("환불 금액 초과");
        }

        return payment;
    }

    @Transactional
    public void refundPayment(
            Long paymentId,
            BigDecimal refundAmount,
            PgCancelResponseDTO pgResponse
    ) {

        Payment payment = paymentRepository.findByPaymentId(paymentId);

        // 1. PG 결과 검증
        if (!"CANCELLED".equals(pgResponse.getStatus())) {
            payment.fail(pgResponse.getMessage());
            throw new PaymentException("PG 환불 실패: " + pgResponse.getMessage());
        }

        // 2. 금액 검증 (방어 로직)
        if (payment.getRemainingAmount().compareTo(refundAmount) < 0) {
            throw new IllegalArgumentException("환불 금액 초과");
        }

        try {
            // 3. 계좌 복구 (핵심)
            transactionService.deposit(
                    new DepositRequestDTO(
                            payment.getAccountId(),
                            refundAmount
                    )
            );

        } catch (Exception e) {
            // 계좌 복구 실패 시 롤백
            throw e;
        }
    }

}
