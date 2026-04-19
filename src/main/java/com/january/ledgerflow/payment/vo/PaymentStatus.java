package com.january.ledgerflow.payment.vo;

import java.util.Set;

public enum PaymentStatus {
    REQUESTED,
    PENDING,
    APPROVED,
    COMPLETED,
    CANCELLED,
    REFUNDED,
    FAILED;

    // 상태 전이 정의 - State Machine
    public boolean canTransitionTo(PaymentStatus target) {
        return switch (this) {
            case REQUESTED -> Set.of(PENDING, FAILED).contains(target);
            case PENDING -> Set.of(APPROVED, FAILED).contains(target);
            case APPROVED -> CANCELLED == target;
            case COMPLETED -> REFUNDED == target;
            case FAILED, CANCELLED, REFUNDED -> false;
        };
    }
}
