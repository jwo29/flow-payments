package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.*;
import com.january.ledgerflow.payment.vo.PaymentMethod;
import com.january.ledgerflow.transaction.dto.TransferRequestDTO;
import com.january.ledgerflow.transaction.service.TransactionService;
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
    public PaymentApproveResponseDTO process(Payment payment, PaymentApproveRequestDTO request) {
        // 1. 즉시 이체
        transactionService.transfer(
                new TransferRequestDTO(
                        request.getAccountId(),
                        request.getMerchantAccountId(),
                        payment.getAmount()
                )
        );

        // 2. 즉시 승인
        payment.approveWithoutPg();

        return new PaymentApproveResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    @Override
    @Transactional
    public PaymentRefundResponseDTO refund(Payment payment, PaymentRefundRequestDTO request) {
        transactionService.transfer(
                new TransferRequestDTO(
                        payment.getMerchantAccountId(),
                        payment.getAccountId(),
                        payment.getAmount()
                )
        );

        // 2. 즉시 승인
        payment.refund(request.getAmount());

        return new PaymentRefundResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }

    @Override
    public PaymentApproveResponseDTO processRetry(Payment payment, PaymentRetryRequestDTO request) {
        // 1. 즉시 이체
        transactionService.transfer(
                new TransferRequestDTO(
                        payment.getAccountId(),
                        payment.getMerchantAccountId(),
                        payment.getAmount()
                )
        );

        // 2. 즉시 승인
        payment.approveWithoutPg();

        return new PaymentApproveResponseDTO(
                payment.getPaymentId(),
                payment.getStatus().name(),
                payment.getOrderId(),
                payment.getAmount()
        );
    }
}
