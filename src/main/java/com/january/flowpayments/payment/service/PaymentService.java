package com.january.flowpayments.payment.service;

import com.january.flowpayments.payment.domain.Payment;
import com.january.flowpayments.payment.domain.PaymentStatus;
import com.january.flowpayments.payment.domain.PaymentTransaction;
import com.january.flowpayments.payment.dto.*;
import com.january.flowpayments.payment.exception.PaymentException;
import com.january.flowpayments.payment.processor.PaymentProcessor;
import com.january.flowpayments.payment.processor.PaymentProcessorFactory;
import com.january.flowpayments.payment.repository.PaymentRepository;
import com.january.flowpayments.payment.repository.PaymentTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final PaymentTransactionRepository txRepository;
    private final PaymentProcessorFactory factory;

    @Transactional
    public PaymentResponseDTO approve(PaymentApproveRequestDTO paymentApproveRequestDTO) {

        // 1. idempotency 체크
        Payment payment = paymentRepository.findByOrderId(paymentApproveRequestDTO.getOrderId())
                .orElseGet(() -> createPayment(paymentApproveRequestDTO));

        // 이미 완료된 결제면 그대로 반환
        if (payment.getStatus() == PaymentStatus.APPROVED) {
            return toResponse(payment);
        }

        // 2. 전략 선택
        PaymentProcessor processor = factory.get(payment.getPaymentMethod());

        // 3. 위임
        PaymentProcessResult paymentProcessResult = processor.process(payment, paymentApproveRequestDTO);

        if (!paymentProcessResult.isSuccess()) {
            payment.fail(paymentProcessResult.getRawResponse());
            // 실패 트랜잭션 저장
            txRepository.save(PaymentTransaction.builder()
                    .paymentId(payment.getPaymentId())
                    .type("APPROVE")
                    .amount(payment.getAmount())
                    .status("FAIL")
                    .pgResponse(paymentProcessResult.getRawResponse())
                    .build());
            throw new PaymentException("결제 실패");
        }

        if (paymentProcessResult.getNextStatus() != PaymentStatus.APPROVED) {
            txRepository.save(PaymentTransaction.builder()
                    .paymentId(payment.getPaymentId())
                    .type("APPROVE")
                    .amount(payment.getAmount())
                    .status("TIMEOUT")
                    .pgResponse(paymentProcessResult.getRawResponse())
                    .build());
            throw new PaymentException("결제 타임아웃");
        }

        try {
            payment.approve(paymentProcessResult.getPgTransactionId(), paymentProcessResult.getAuthCode());
            payment.capture(); // 즉시결제 모델

            txRepository.save(PaymentTransaction.builder()
                    .paymentId(payment.getPaymentId())
                    .type("APPROVE")
                    .amount(payment.getAmount())
                    .status("SUCCESS")
                    .pgResponse(paymentProcessResult.getRawResponse())
                    .build());
        } catch (Exception e) {
            // 보상 트랜잭션
            compensateApprove(payment, paymentProcessResult);
            throw e;
        }

        return toResponse(payment);
    }

    private void compensateApprove(Payment payment, PaymentProcessResult result) {
        try {
            PaymentProcessor processor = factory.get(payment.getPaymentMethod());
            // PG 취소 요청
            processor.refund(payment, new PaymentRefundRequestDTO(
                    payment.getPaymentId(),
                    payment.getAmount(),
                    result.getRawResponse()
            ));

            // 상태 변경
            payment.cancel();

            txRepository.save(PaymentTransaction.builder()
                    .paymentId(payment.getPaymentId())
                    .type("COMPENSATE_CANCEL")
                    .status("SUCCESS")
                    .build());
        } catch (Exception e) {
            // 보상 실패 시 기록하여 재처리 진행
            txRepository.save(PaymentTransaction.builder()
                    .paymentId(payment.getPaymentId())
                    .type("COMPENSATE_CANCEL")
                    .status("fail")
                    .build());

            // 상태 표시 (비정상)
            payment.markCompensationFailed();
        }
    }

    private Payment createPayment(PaymentApproveRequestDTO paymentApproveRequestDTO) {
        try {
            Payment payment = new Payment(
            );

            return paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            // 동시에 다른 요청이 먼저 insert한 경우
            return paymentRepository.findByOrderId(paymentApproveRequestDTO.getOrderId())
                    .orElseThrow();
        }
    }

    private PaymentResponseDTO toResponse(Payment payment) {
        return new PaymentResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    public PaymentResponseDTO refund(PaymentRefundRequestDTO paymentRefundRequestDTO) {

        Payment payment = paymentTransactionService.getRefundablePayment(paymentRefundRequestDTO.getPaymentId(), paymentRefundRequestDTO.getAmount());

        // 전략 선택
        PaymentProcessor processor = factory.get(payment.getPaymentMethod());

        PaymentProcessResult paymentProcessResult = processor.refund(payment, paymentRefundRequestDTO);

        // Payment 상태 및 금액 반영
        payment.refund(paymentRefundRequestDTO.getAmount());

        txRepository.save(PaymentTransaction.builder()
                .paymentId(payment.getPaymentId())
                .type("REFUND")
                .amount(payment.getAmount())
                .status("SUCCESS")
                .pgResponse(paymentProcessResult.getRawResponse())
                .build());

        return toResponse(payment);
    }

    public PaymentResponseDTO cancel(PaymentRefundRequestDTO paymentRefundRequestDTO) {
        return refund(paymentRefundRequestDTO);
    }

    @Transactional
    public PaymentResponseDTO retry (Long paymentId, PaymentRetryRequestDTO paymentRetryRequestDTO) {

        Payment payment = paymentRepository.findByPaymentId(paymentId);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("재시도 불가 상태");
        }

        PaymentProcessor processor = factory.get(payment.getPaymentMethod());

        PaymentProcessResult paymentProcessResult = processor.processRetry(payment, paymentRetryRequestDTO);

        payment.approve();
        payment.capture();

        txRepository.save(PaymentTransaction.builder()
                .paymentId(payment.getPaymentId())
                .type("REFUND")
                .amount(payment.getAmount())
                .status("SUCCESS")
                .pgResponse(paymentProcessResult.getRawResponse())
                .build());

        return toResponse(payment);
    }

    public void reconcile(Long paymentId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId);

        PaymentProcessor processor = factory.get(payment.getPaymentMethod());

        PaymentProcessResult paymentProcessResult = processor.inquiry(payment.getPgTransactionId());

        if (paymentProcessResult.isSuccess()) {
            payment.capture();
        } else {
            payment.cancel();
        }
    }

}
