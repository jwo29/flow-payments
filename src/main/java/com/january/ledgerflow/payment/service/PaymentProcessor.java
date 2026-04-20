package com.january.ledgerflow.payment.service;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.*;
import com.january.ledgerflow.payment.vo.PaymentMethod;

public interface PaymentProcessor {

    PaymentMethod support();

    PaymentApproveResponseDTO process(
            Payment payment,
            PaymentApproveRequestDTO request
    );

    PaymentRefundResponseDTO refund(
            Payment payment,
            PaymentRefundRequestDTO request
    );

    PaymentApproveResponseDTO processRetry(
            Payment payment,
            PaymentRetryRequestDTO request
    );

}
