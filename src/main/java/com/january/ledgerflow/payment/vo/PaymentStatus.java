package com.january.ledgerflow.payment.vo;

import java.util.Set;

public enum PaymentStatus {
    REQUESTED,
    PENDING,
    APPROVED,
    PARTIALLY_REFUNDED,
    REFUNDED,
    FAILED;

    // 상태 전이 정의 - State Machine
    public boolean canTransitionTo(PaymentStatus target) {
        return switch (this) {
            case REQUESTED -> Set.of(PENDING, FAILED).contains(target);
            case PENDING -> Set.of(APPROVED, FAILED).contains(target);
            case APPROVED -> Set.of(PARTIALLY_REFUNDED, REFUNDED).contains(target);
            case PARTIALLY_REFUNDED -> Set.of(PARTIALLY_REFUNDED, REFUNDED).contains(target);
            default -> false;
        };
    }

    public boolean canRefund() {
        return this == APPROVED || this == PARTIALLY_REFUNDED;
    }
}
