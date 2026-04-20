package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.*;
import com.january.ledgerflow.payment.repository.PaymentRepository;
import com.january.ledgerflow.payment.vo.PaymentMethod;
import com.january.ledgerflow.payment.vo.PaymentStatus;
import jakarta.transaction.Transactional;
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

    private final PaymentTransactionService paymentTransactionService;
    private final PaymentRepository paymentRepository;
    private final PaymentProcessorFactory factory;

    @Transactional
    public PaymentApproveResponseDTO approve(PaymentApproveRequestDTO paymentApproveRequestDTO) {

        // 1. idempotency 체크
        Payment payment = paymentTransactionService.getOrCreatePayment(paymentApproveRequestDTO);

        // 이미 완료된 결제면 그대로 반환
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            return toResponse(payment);
        }

        // 2. 전략 선택
        PaymentProcessor processor = factory.get(payment.getPaymentMethod());

        // 3. 위임
        return processor.process(payment, paymentApproveRequestDTO);

    }

    private PaymentApproveResponseDTO toResponse(Payment payment) {
        return new PaymentApproveResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    public PaymentRefundResponseDTO refund(PaymentRefundRequestDTO paymentRefundRequestDTO) {

        Payment payment = paymentTransactionService.getRefundablePayment(paymentRefundRequestDTO.getPaymentId(), paymentRefundRequestDTO.getAmount());

        // 2. 전략 선택
        PaymentProcessor processor = factory.get(payment.getPaymentMethod());

        return processor.refund(payment, paymentRefundRequestDTO);
    }

    public PaymentRefundResponseDTO cancel(PaymentRefundRequestDTO paymentRefundRequestDTO) {
        return refund(paymentRefundRequestDTO);
    }

    @Transactional
    public PaymentApproveResponseDTO retry (Long paymentId, PaymentRetryRequestDTO paymentRetryRequestDTO) {

        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("재시도 불가 상태");
        }

        PaymentProcessor processor = factory.get(payment.getPaymentMethod());

        return processor.processRetry(payment, paymentRetryRequestDTO);
    }

    private PaymentApproveRequestDTO toRequest(Payment payment, PaymentRetryRequestDTO paymentRetryRequestDTO) {
        return new PaymentApproveRequestDTO(
                payment.getMerchantId(),
                payment.getUserId(),
                payment.getAccountId(),
                null,
                payment.getAmount(),
                payment.getOrderId(),
                paymentRetryRequestDTO.getCardNumber(),
                paymentRetryRequestDTO.getInstallment(),
                PaymentMethod.CARD
        );
    }

}
