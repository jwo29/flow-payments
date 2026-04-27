package com.january.ledgerflow.payment.processor;

import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.domain.PaymentMethod;
import com.january.ledgerflow.payment.dto.PaymentApproveRequestDTO;
import com.january.ledgerflow.payment.dto.PaymentProcessResult;
import com.january.ledgerflow.payment.dto.PaymentRefundRequestDTO;
import com.january.ledgerflow.payment.dto.PaymentRetryRequestDTO;

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
