package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.*;
import com.january.ledgerflow.payment.vo.PaymentMethod;
import com.january.ledgerflow.payment.vo.PaymentStatus;
import com.january.ledgerflow.pg.PgClient;
import com.january.ledgerflow.pg.dto.PgApproveRequestDTO;
import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import com.january.ledgerflow.pg.dto.PgCancelRequestDTO;
import com.january.ledgerflow.pg.dto.PgCancelResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentProcessor implements PaymentProcessor {

    private final PgClient pgClient;
    private final PaymentTransactionService paymentTransactionService;

    @Override
    public PaymentMethod support() {
        return PaymentMethod.CARD;
    }

    @Override
    @Transactional
    public PaymentApproveResponseDTO process(Payment payment, PaymentApproveRequestDTO request) {

        // 1. PENDING
        payment.markPending();

        try {
            // 3. PG 요청
            PgApproveRequestDTO pgApproveRequestDTO = new PgApproveRequestDTO(
                    request.getMerchantId(),
                    request.getOrderId(),
                    request.getAmount(),
                    request.getCardNumber(),
                    request.getInstallment()
            );
            PgApproveResponseDTO pgApproveResponseDTO = pgClient.approve(pgApproveRequestDTO);

            // 4. 결과 반영
            paymentTransactionService.completePayment(payment.getPaymentId(), pgApproveResponseDTO);
        } catch (Exception e) {
            // 예외 발생 시, 실패 확정하지 않고, PENDING 유지
            return new PaymentApproveResponseDTO(
                    payment.getPaymentId(),
                    PaymentStatus.PENDING.name(),
                    payment.getOrderId(),
                    payment.getAmount()
            );
        }

        return toResponse(payment);
    }

    @Override
    public PaymentRefundResponseDTO refund(Payment payment, PaymentRefundRequestDTO request) {
        // 1. PG 요청 DTO로 변환
        PgCancelRequestDTO pgCancelRequestDTO = new PgCancelRequestDTO(
                payment.getPgTransactionId(),
                payment.getAmount(),
                request.getReason()
        );

        // PG 호출
        PgCancelResponseDTO pgCancelResponseDTO = pgClient.cancel(pgCancelRequestDTO);

        paymentTransactionService.refundPayment(payment.getPaymentId(), request.getAmount(), pgCancelResponseDTO);

        return new PaymentRefundResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    @Override
    public PaymentApproveResponseDTO processRetry(Payment payment, PaymentRetryRequestDTO request) {
        try {
            // 3. PG 요청
            PgApproveRequestDTO pgApproveRequestDTO = new PgApproveRequestDTO(
                    payment.getMerchantId(),
                    payment.getOrderId(),
                    payment.getAmount(),
                    request.getCardNumber(),
                    request.getInstallment()
            );
            PgApproveResponseDTO pgApproveResponseDTO = pgClient.approve(pgApproveRequestDTO);

            // 4. 결과 반영
            paymentTransactionService.completePayment(payment.getPaymentId(), pgApproveResponseDTO);
        } catch (Exception e) {
            // 예외 발생 시, 실패 확정하지 않고, PENDING 유지
            return new PaymentApproveResponseDTO(
                    payment.getPaymentId(),
                    PaymentStatus.PENDING.name(),
                    payment.getOrderId(),
                    payment.getAmount()
            );
        }

        return toResponse(payment);
    }

    private PaymentApproveResponseDTO toResponse(Payment payment) {
        return new PaymentApproveResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }
}
