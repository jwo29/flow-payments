package com.january.ledgerflow.payment.domain;

import com.january.ledgerflow.payment.vo.PaymentMethod;
import com.january.ledgerflow.payment.vo.PaymentStatus;
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
        changeStatus(PaymentStatus.PENDING);
    }

    public void approve(String pgTxId, String authCode) {
        changeStatus(PaymentStatus.APPROVED);
        this.pgTransactionId = pgTxId;
        this.authCode = authCode;
        this.approvedAt = LocalDateTime.now();
    }

    public void approveWithoutPg() {
        changeStatus(PaymentStatus.APPROVED);
        this.approvedAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        changeStatus(PaymentStatus.FAILED);
        this.failureReason = reason;
    }

    public void refund(BigDecimal amount) {
        this.refundedAmount = this.refundedAmount.add(amount);

        if (getRemainingAmount().compareTo(BigDecimal.ZERO) == 0) {
            changeStatus(PaymentStatus.REFUNDED);
        } else {
            changeStatus(PaymentStatus.PARTIALLY_REFUNDED);
        }
    }

    public BigDecimal getRemainingAmount() {
        return amount.subtract(refundedAmount);
    }

    private void changeStatus(PaymentStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid status transition: " + this.status + " → " + target);
        }
        this.status = target;
    }


}
