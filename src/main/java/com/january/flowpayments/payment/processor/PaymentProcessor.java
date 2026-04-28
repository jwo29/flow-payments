package com.january.flowpayments.payment.processor;

import com.january.flowpayments.payment.domain.Payment;
import com.january.flowpayments.payment.domain.PaymentMethod;
import com.january.flowpayments.payment.dto.PaymentApproveRequestDTO;
import com.january.flowpayments.payment.dto.PaymentProcessResult;
import com.january.flowpayments.payment.dto.PaymentRefundRequestDTO;
import com.january.flowpayments.payment.dto.PaymentRetryRequestDTO;

public interface PaymentProcessor {

    PaymentMethod support();

    PaymentProcessResult process(
            Payment payment,
            PaymentApproveRequestDTO request
    );

    PaymentProcessResult refund(
            Payment payment,
            PaymentRefundRequestDTO request
    );

    PaymentProcessResult processRetry(
            Payment payment,
            PaymentRetryRequestDTO request
    );

    PaymentProcessResult inquiry(String pgTransactionId);

}
