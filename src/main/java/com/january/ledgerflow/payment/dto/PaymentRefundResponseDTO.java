package com.january.ledgerflow.payment.dto;

import lombok.Getter;

@Getter
public class PaymentRefundResponseDTO {
    private Long paymentId;
    private String paymentStatus;
    private Integer refundAmount;
}
