package com.january.ledgerflow.payment.dto;

import com.january.ledgerflow.payment.domain.PaymentStatus;
import lombok.Getter;

@Getter
public class PaymentProcessResult {
    private final String pgTransactionId;
    private final String authCode;
    private final String rawResponse;
    private final PaymentStatus nextStatus;

    public PaymentProcessResult(String pgTransactionId, String authCode, String rawResponse, PaymentStatus nextStatus) {
        this.pgTransactionId = pgTransactionId;
        this.authCode = authCode;
        this.rawResponse = rawResponse;
        this.nextStatus = nextStatus;
    }

    public PaymentProcessResult(PaymentStatus nextStatus, String rawResponse) {
        this.pgTransactionId = null;
        this.authCode = null;
        this.nextStatus = nextStatus;
        this.rawResponse = rawResponse;
    }

    public PaymentProcessResult(PaymentStatus nextStatus) {
        this.pgTransactionId = null;
        this.authCode = null;
        this.nextStatus = nextStatus;
        this.rawResponse = null;
    }
}
