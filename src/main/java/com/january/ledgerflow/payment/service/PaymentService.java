package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.*;
import com.january.ledgerflow.payment.exception.PaymentException;
import com.january.ledgerflow.payment.repository.PaymentRepository;
import com.january.ledgerflow.pg.PgClient;
import com.january.ledgerflow.pg.dto.PgApproveRequestDTO;
import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import com.january.ledgerflow.pg.dto.PgCancelRequestDTO;
import com.january.ledgerflow.pg.dto.PgCancelResponseDTO;
import com.january.ledgerflow.transaction.dto.DepositRequestDTO;
import com.january.ledgerflow.transaction.dto.WithdrawRequestDTO;
import com.january.ledgerflow.transaction.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PgClient pgClient;
    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;

    @Transactional
    public PaymentApproveResponseDTO approve(PaymentApproveRequestDTO paymentApproveRequestDTO) {

        // 1. PG 요청 DTO로 변환
        PgApproveRequestDTO pgApproveRequestDTO = new PgApproveRequestDTO(
                paymentApproveRequestDTO.getMerchantId(),
                paymentApproveRequestDTO.getOrderId(),
                paymentApproveRequestDTO.getAmount(),
                paymentApproveRequestDTO.getCardNumber(),
                paymentApproveRequestDTO.getInstallment()
        );

        // 2. PG 호출
        PgApproveResponseDTO pgApproveResponseDTO = pgClient.approve(pgApproveRequestDTO);

        if (!"APPROVED".equals(pgApproveResponseDTO.getStatus())) {
            throw new PaymentException("PG 승인 실패: " + pgApproveResponseDTO.getMessage());
        }

        // 3. Payment 생성 (상태: COMPLETE)
        Payment payment = Payment.approve(
                paymentApproveRequestDTO.getMerchantId(),
                paymentApproveRequestDTO.getUserId(),
                paymentApproveRequestDTO.getAccountId(),
                paymentApproveRequestDTO.getAmount(),
                paymentApproveRequestDTO.getOrderId(),
                pgApproveResponseDTO.getPgTransactionId(),
                pgApproveResponseDTO.getAuthCode()
        );

        // 4. 내부 결제 처리
        paymentRepository.save(payment);

        // 5. 계좌 잔액 차감
        transactionService.withdraw(
                new WithdrawRequestDTO(
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );

        return new PaymentApproveResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    @Transactional
    public PaymentCancelResponseDTO cancel(PaymentCancelRequestDTO paymentCancelRequestDTO) {

        Payment payment = paymentRepository.findByPaymentId(paymentCancelRequestDTO.getPaymentId());

        if (!payment.isApproved()) {
            throw new IllegalStateException("취소 불가 상태: ");
        }

        // 1. PG 요청 DTO로 변환
        PgCancelRequestDTO pgCancelRequestDTO = new PgCancelRequestDTO(
                payment.getPgTransactionId(),
                payment.getAmount(),
                paymentCancelRequestDTO.getReason()
        );

        // PG 호출
        PgCancelResponseDTO pgCancelResponseDTO = pgClient.cancel(pgCancelRequestDTO);

        if (!"CANCELED".equals(pgCancelResponseDTO.getStatus())) {
            payment.fail(pgCancelResponseDTO.getMessage());
            paymentRepository.save(payment);

            throw new PaymentException("PG 취소 실패: " + pgCancelResponseDTO.getMessage());
        }

        // 3. Payment 생성 (상태: CANCELLED)
        payment.cancel();

        // 4. 내부 취소 처리
        paymentRepository.save(payment);

        // 5. 계좌 잔액 차감
        transactionService.deposit(
                new DepositRequestDTO(
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );

        return new PaymentCancelResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );

    }

    @Transactional
    public PaymentRefundResponseDTO refund(PaymentRefundRequestDTO paymentRefundRequestDTO) {
        Payment payment = paymentRepository.findByPaymentId(paymentRefundRequestDTO.getPaymentId());

        if (!payment.isCompleted()) {
            throw new IllegalStateException("환불 불가 상태");
        }

        // 1. PG 요청 DTO로 변환
        PgCancelRequestDTO pgCancelRequestDTO = new PgCancelRequestDTO(
                payment.getPgTransactionId(),
                payment.getAmount(),
                paymentRefundRequestDTO.getReason()
        );

        // PG 호출
        PgCancelResponseDTO pgCancelResponseDTO = pgClient.cancel(pgCancelRequestDTO);

        if (!"CANCELED".equals(pgCancelResponseDTO.getStatus())) {
            payment.fail(pgCancelResponseDTO.getMessage());
            paymentRepository.save(payment);

            throw new PaymentException("PG 취소 실패: " + pgCancelResponseDTO.getMessage());
        }

        // 3. Payment 생성 (상태: CANCELLED)
        payment.cancel();

        // 4. 내부 취소 처리
        paymentRepository.save(payment);

        // 5. 계좌 잔액 차감
        transactionService.deposit(
                new DepositRequestDTO(
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );

        return new PaymentRefundResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

}
