package com.january.flowpayments.payment.domain;

public enum PaymentStatus {
    REQUESTED,
    PENDING,
    APPROVED,
    CAPTURED,
    CANCELED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    FAILED,
    UNKNOWN;
}
