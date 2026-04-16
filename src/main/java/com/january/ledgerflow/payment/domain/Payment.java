package com.january.ledgerflow.payment.domain;

import com.january.ledgerflow.payment.dto.PaymentApproveRequestDTO;
import com.january.ledgerflow.payment.vo.PaymentStatus;
import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
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

    public static Payment request(PaymentApproveRequestDTO requestDTO) {
        Payment p = new Payment();
        p.merchantId = requestDTO.getMerchantId();
        p.userId = requestDTO.getUserId();
        p.accountId = requestDTO.getAccountId();
        p.orderId = requestDTO.getOrderId();
        p.amount = requestDTO.getAmount();
        p.status = PaymentStatus.REQUESTED;
        p.createdAt = LocalDateTime.now();

        return p;
    }

    /* ==========================
           Factory Method
       ========================== */
    public void approve(PgApproveResponseDTO responseDTO) {
        changeStatus(PaymentStatus.APPROVED);
        this.pgTransactionId = responseDTO.getPgTransactionId();
        this.authCode = responseDTO.getAuthCode();
        this.approvedAt = LocalDateTime.now();
    }

    public void cancel() {
        changeStatus(PaymentStatus.CANCELLED);
        this.canceledAt = LocalDateTime.now();
    }

    public void fail(String reason) {
        changeStatus(PaymentStatus.FAILED);
        this.failureReason = reason;
    }

    public void refund() {
        changeStatus(PaymentStatus.REFUNDED);
    }

    public void changeStatus(PaymentStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new IllegalStateException("Invalid status transition: " + this.status + " → " + target);
        }
        this.status = target;
    }

}
