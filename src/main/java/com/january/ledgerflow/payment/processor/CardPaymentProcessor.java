package com.january.ledgerflow.payment.processor;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.domain.PaymentMethod;
import com.january.ledgerflow.payment.domain.PaymentStatus;
import com.january.ledgerflow.payment.dto.PaymentApproveRequestDTO;
import com.january.ledgerflow.payment.dto.PaymentProcessResult;
import com.january.ledgerflow.payment.dto.PaymentRefundRequestDTO;
import com.january.ledgerflow.payment.dto.PaymentRetryRequestDTO;
import com.january.ledgerflow.payment.service.PaymentTransactionService;
import com.january.ledgerflow.pg.PgClient;
import com.january.ledgerflow.pg.dto.*;
import io.netty.handler.timeout.TimeoutException;
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
    public PaymentProcessResult process(Payment payment, PaymentApproveRequestDTO request) {

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

            return new PaymentProcessResult(
                    true,
                    pgApproveResponseDTO.getPgTransactionId(),
                    pgApproveResponseDTO.getAuthCode(),
                    pgApproveResponseDTO.toString(),
                    PaymentStatus.APPROVED
            );
        } catch (TimeoutException e) {
            payment.markUnknown();
            return new PaymentProcessResult(
                    true,
                    PaymentStatus.UNKNOWN
            );
        } catch (Exception e) {
            // 예외 발생 시, 실패 확정하지 않고, PENDING 유지
            return new PaymentProcessResult(
                    false,
                    PaymentStatus.PENDING
            );
        }
    }

    @Override
    public PaymentProcessResult refund(Payment payment, PaymentRefundRequestDTO request) {
        // 1. PG 요청 DTO로 변환
        PgCancelRequestDTO pgCancelRequestDTO = new PgCancelRequestDTO(
                payment.getPgTransactionId(),
                payment.getAmount(),
                request.getReason()
        );

        // PG 호출
        PgCancelResponseDTO pgCancelResponseDTO = pgClient.cancel(pgCancelRequestDTO);

        paymentTransactionService.refundPayment(payment.getPaymentId(), request.getAmount(), pgCancelResponseDTO);

        return new PaymentProcessResult(
                true,
                PaymentStatus.REFUNDED,
                pgCancelResponseDTO.toString()
        );
    }

    @Override
    public PaymentProcessResult processRetry(Payment payment, PaymentRetryRequestDTO request) {
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

            return new PaymentProcessResult(
                    true,
                    PaymentStatus.PENDING,
                    pgApproveResponseDTO.toString()
            );
        } catch (Exception e) {
            // 예외 발생 시, 실패 확정하지 않고, PENDING 유지
            return new PaymentProcessResult(
                    false,
                    PaymentStatus.PENDING
            );
        }
    }

    @Override
    public PaymentProcessResult inquiry(String pgTransactionId) {
        // mock pg 호출
        PgInquiryResponseDTO pgInquiryResponseDTO = pgClient.inquiry(pgTransactionId);
        return new PaymentProcessResult(
                true,
                pgInquiryResponseDTO.getPgTransactionId(),
                pgInquiryResponseDTO.getAuthCode(),
                pgInquiryResponseDTO.toString(),
                PaymentStatus.APPROVED
        );
    }
}
