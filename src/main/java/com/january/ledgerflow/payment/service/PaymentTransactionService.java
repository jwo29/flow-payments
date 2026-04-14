package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.PaymentApproveRequestDTO;
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

@Service
@RequiredArgsConstructor
public class PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;

    @Transactional
    public Payment createPayment(PaymentApproveRequestDTO paymentApproveRequestDTO) {
        Payment payment = Payment.request(paymentApproveRequestDTO);
        return paymentRepository.save(payment);
    }

    @Transactional
    public void completePayment(Long paymentId, PgApproveResponseDTO pgApproveResponseDTO) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (!"APPROVED".equals(pgApproveResponseDTO.getStatus())) {
            payment.fail(pgApproveResponseDTO.getMessage());

            throw new PaymentException("PG 승인 실패: " + pgApproveResponseDTO.getMessage());
        }

        payment.approve(pgApproveResponseDTO);

        // 5. 계좌 잔액 차감
        transactionService.withdraw(
                new WithdrawRequestDTO(
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );

    }

    @Transactional
    public Payment getCancelablePayment(Long paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (!payment.isApproved()) {
            throw new IllegalStateException("취소 불가 상태");
        }

        return payment;
    }

    @Transactional
    public void cancelPayment(Long paymendId, PgCancelResponseDTO pgCancelResponseDTO) {
        Payment payment = paymentRepository.findByPaymentId(paymendId);

        if (!"CANCELED".equals(pgCancelResponseDTO.getStatus())) {
            payment.fail(pgCancelResponseDTO.getMessage());

            throw new PaymentException("PG 취소 실패: " + pgCancelResponseDTO.getMessage());
        }

        // 3. Payment 생성 (상태: CANCELLED)
        payment.cancel();

        // 5. 계좌 잔액 차감
        transactionService.deposit(
                new DepositRequestDTO(
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );
    }

}
