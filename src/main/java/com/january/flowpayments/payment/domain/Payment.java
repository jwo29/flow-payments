package com.january.flowpayments.payment.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String merchantId;
    private Long userId;
    private Long accountId;
    private Long merchantAccountId;

    private String orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String pgTransactionId; // PG 거래 ID
    private String authCode;        // 승인 코드
    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;

    public Payment(String merchantId, Long userId, Long accountId, String orderId, BigDecimal amount, PaymentMethod paymentMethod) {
    }

    public void markPending() {
        this.status = PaymentStatus.PENDING;
    }

    public void markCompensationFailed() {
        this.status = PaymentStatus.FAILED;
    }

    public void markUnknown() {
        this.status = PaymentStatus.UNKNOWN;
    }

    public boolean canApprove() {
        return this.status == PaymentStatus.REQUESTED;
    }

    public boolean canCapture() {
        return this.status == PaymentStatus.APPROVED;
    }

    public boolean canCancel() {
        return this.status == PaymentStatus.APPROVED;
    }

    public boolean canRefund() {
        return this.status == PaymentStatus.CAPTURED;
    }


    public void approve(String pgTxId, String authCode) {
        if (!canApprove()) {
            throw new IllegalStateException();
        }
        this.status = PaymentStatus.APPROVED;
        this.pgTransactionId = pgTxId;
        this.authCode = authCode;
        this.approvedAt = LocalDateTime.now();
    }

    public void approve() {
        approve(null, null);
    }

    public void capture() {
        if (!canCapture()) {
            throw new IllegalStateException();
        }
        this.status = PaymentStatus.CAPTURED;
    }

    public void cancel() {
        if (!canCancel()) {
            throw new IllegalStateException();
        }
        this.status = PaymentStatus.CANCELED;
        this.canceledAt = LocalDateTime.now();
    }

    public void refund() {
        if (!canRefund()) {
            throw new IllegalStateException();
        }
        this.status = PaymentStatus.REFUNDED;
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public void refund(BigDecimal amount) {
        this.refundedAmount = this.refundedAmount.add(amount);

        if (getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            this.status = PaymentStatus.REFUNDED;
        } else {
            this.status = PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    public BigDecimal getRemainingAmount() {
        return amount.subtract(refundedAmount);
    }



}
