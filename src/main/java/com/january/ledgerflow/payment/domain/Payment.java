package com.january.ledgerflow.payment.domain;

import com.january.ledgerflow.payment.vo.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    private String merchantId;
    private Long userId;
    private Long accountId;

    private String orderId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    private String pgTransactionId; // PG 거래 ID
    private String authCode;        // 승인 코드

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String failureReason;

    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime canceledAt;

    /* ==========================
           Factory Method
       ========================== */
    public static Payment approve(String merchantId,
                                   Long userId,
                                   Long accountId,
                                   BigDecimal amount,
                                   String orderId,
                                   String pgTransactionId,
                                   String authCode) {
        Payment p = new Payment();
        p.merchantId = merchantId;
        p.userId = userId;
        p.accountId = accountId;
        p.amount = amount;
        p.orderId = orderId;
        p.pgTransactionId = pgTransactionId;
        p.authCode = authCode;
        p.status = PaymentStatus.COMPLETED;

        return p;
    }

    public void cancel() {
        this.status = PaymentStatus.CANCELLED;
        this.canceledAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    public boolean isRequested() {
        return PaymentStatus.REQUESTED == this.status;
    }

    public boolean isApproved() {
        return PaymentStatus.APPROVED == this.status;
    }

    public boolean isCompleted() {
        return PaymentStatus.COMPLETED == this.status;
    }

    public boolean isCanceled() {
        return PaymentStatus.CANCELLED == this.status;
    }

    public boolean isFailed() {
        return PaymentStatus.FAILED == this.status;
    }
}
