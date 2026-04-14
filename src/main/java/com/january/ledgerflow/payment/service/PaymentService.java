package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.*;
import com.january.ledgerflow.payment.repository.PaymentRepository;
import com.january.ledgerflow.pg.PgClient;
import com.january.ledgerflow.pg.dto.PgApproveRequestDTO;
import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import com.january.ledgerflow.pg.dto.PgCancelRequestDTO;
import com.january.ledgerflow.pg.dto.PgCancelResponseDTO;
import com.january.ledgerflow.transaction.service.TransactionService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PgClient pgClient;
    private final PaymentRepository paymentRepository;
    private final TransactionService transactionService;

    private final PaymentTransactionService paymentTransactionService;

    public PaymentApproveResponseDTO approve(PaymentApproveRequestDTO paymentApproveRequestDTO) {

        Payment payment = paymentTransactionService.createPayment(paymentApproveRequestDTO);

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

        // 3. 결과 반영
        paymentTransactionService.completePayment(payment.getPaymentId(), pgApproveResponseDTO);

        return new PaymentApproveResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    public PaymentCancelResponseDTO cancel(PaymentCancelRequestDTO paymentCancelRequestDTO) {


        Payment payment = paymentTransactionService.getCancelablePayment(paymentCancelRequestDTO.getPaymentId());

        // 1. PG 요청 DTO로 변환
        PgCancelRequestDTO pgCancelRequestDTO = new PgCancelRequestDTO(
                payment.getPgTransactionId(),
                payment.getAmount(),
                paymentCancelRequestDTO.getReason()
        );

        // PG 호출
        PgCancelResponseDTO pgCancelResponseDTO = pgClient.cancel(pgCancelRequestDTO);

        paymentTransactionService.cancelPayment(payment.getPaymentId(), pgCancelResponseDTO);

        return new PaymentCancelResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );

    }

    public PaymentRefundResponseDTO refund(PaymentRefundRequestDTO paymentRefundRequestDTO) {

        Payment payment = paymentTransactionService.getCancelablePayment(paymentRefundRequestDTO.getPaymentId());

        // 1. PG 요청 DTO로 변환
        PgCancelRequestDTO pgCancelRequestDTO = new PgCancelRequestDTO(
                payment.getPgTransactionId(),
                payment.getAmount(),
                paymentRefundRequestDTO.getReason()
        );

        // PG 호출
        PgCancelResponseDTO pgCancelResponseDTO = pgClient.cancel(pgCancelRequestDTO);

        paymentTransactionService.cancelPayment(payment.getPaymentId(), pgCancelResponseDTO);

        return new PaymentRefundResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

}
