package com.january.flowpayments.payment.processor;

import com.january.flowpayments.payment.domain.Payment;
import com.january.flowpayments.payment.domain.PaymentMethod;
import com.january.flowpayments.payment.domain.PaymentStatus;
import com.january.flowpayments.payment.dto.PaymentApproveRequestDTO;
import com.january.flowpayments.payment.dto.PaymentProcessResult;
import com.january.flowpayments.payment.dto.PaymentRefundRequestDTO;
import com.january.flowpayments.payment.dto.PaymentRetryRequestDTO;
import com.january.flowpayments.transaction.dto.TransferRequestDTO;
import com.january.flowpayments.transaction.service.TransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BankTransferPaymentProcessor implements PaymentProcessor {

    private final TransactionService transactionService;

    @Override
    public PaymentMethod support() {
        return PaymentMethod.BANK_TRANSFER;
    }

    @Override
    @Transactional
    public PaymentProcessResult process(Payment payment, PaymentApproveRequestDTO request) {
        // 1. 즉시 이체
        transactionService.transfer(
                new TransferRequestDTO(
                        request.getAccountId(),
                        request.getMerchantAccountId(),
                        payment.getAmount()
                )
        );

        // 2. 즉시 승인
        payment.capture();

        return new PaymentProcessResult(true, PaymentStatus.CAPTURED);
    }

    @Override
    @Transactional
    public PaymentProcessResult refund(Payment payment, PaymentRefundRequestDTO request) {
        transactionService.transfer(
                new TransferRequestDTO(
                        payment.getMerchantAccountId(),
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );

        // 2. 즉시 승인
        payment.refund(request.getAmount());

        return new PaymentProcessResult(true, PaymentStatus.REFUNDED);
    }

    @Override
    public PaymentProcessResult processRetry(Payment payment, PaymentRetryRequestDTO request) {
        // 1. 즉시 이체
        transactionService.transfer(
                new TransferRequestDTO(
                        payment.getAccountId(),
                        payment.getMerchantAccountId(),
                        payment.getAmount()
                )
        );

        // 2. 즉시 승인
        payment.capture();

        return new PaymentProcessResult(true, PaymentStatus.CAPTURED);
    }

    @Override
    public PaymentProcessResult inquiry(String pgTransactionId) {
        return null;
    }
}
