package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.PaymentApproveRequestDTO;
import com.january.ledgerflow.payment.dto.PaymentApproveResponseDTO;
import com.january.ledgerflow.payment.dto.PaymentRefundRequestDTO;
import com.january.ledgerflow.payment.dto.PaymentRefundResponseDTO;
import com.january.ledgerflow.payment.vo.PaymentStatus;
import com.january.ledgerflow.pg.PgClient;
import com.january.ledgerflow.pg.dto.PgApproveRequestDTO;
import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import com.january.ledgerflow.pg.dto.PgCancelRequestDTO;
import com.january.ledgerflow.pg.dto.PgCancelResponseDTO;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 국내 대부분의 이커머스가 사용하는 "즉시 결제" 모델을 구현한다.
 * 국내 PG는 "결제 완료 = 매입 완료"를 보장하기 위해 승인과 매입을 하나로 묶는다.
 *
 * 즉, 결제 요청 시 PG사에서 카드사로의 결제 승인요청(approve)과 매입(capture)까지 한 번에 진행하며,
 * 결제사(해당 프로그램) 입장에서는 항상 capture까지 완료된 결과를 받게 된다.
 *
 * 이로 인해 사실상 cancel과 refund가 동일한 프로세스를 진행하게 된다.
 */
@Service
@AllArgsConstructor
public class PaymentService {

    private final PgClient pgClient;

    private final PaymentTransactionService paymentTransactionService;

    public PaymentApproveResponseDTO approve(PaymentApproveRequestDTO paymentApproveRequestDTO) {

        Payment payment = paymentTransactionService.getOrCreatePayment(paymentApproveRequestDTO);

        if (!(payment.getStatus() == PaymentStatus.APPROVED)) {
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
        }

        return new PaymentApproveResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    public PaymentRefundResponseDTO cancel(PaymentRefundRequestDTO paymentRefundRequestDTO) {
        return refund(paymentRefundRequestDTO);
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
