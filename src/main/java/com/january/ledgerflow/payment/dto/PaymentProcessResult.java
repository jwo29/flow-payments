package com.january.ledgerflow.payment.dto;

import com.january.ledgerflow.payment.domain.PaymentStatus;
import lombok.Getter;

@Getter
public class PaymentProcessResult {
    private final boolean success;
    private final String pgTransactionId;
    private final String authCode;
    private final String rawResponse;
    private final PaymentStatus nextStatus;

    public PaymentProcessResult(boolean success, String pgTransactionId, String authCode, String rawResponse, PaymentStatus nextStatus) {
        this.success = success;
        this.pgTransactionId = pgTransactionId;
        this.authCode = authCode;
        this.rawResponse = rawResponse;
        this.nextStatus = nextStatus;
    }

    public PaymentProcessResult(boolean success, PaymentStatus nextStatus, String rawResponse) {
        this.success = success;
        this.pgTransactionId = null;
        this.authCode = null;
        this.nextStatus = nextStatus;
        this.rawResponse = rawResponse;
    }

    public PaymentProcessResult(boolean success, PaymentStatus nextStatus) {
        this.success = success;
        this.pgTransactionId = null;
        this.authCode = null;
        this.nextStatus = nextStatus;
        this.rawResponse = null;
    }
}
