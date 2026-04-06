package com.january.ledgerflow.payment.dto;

import lombok.Getter;

@Getter
public class PaymentResponseDTO {
    private Long paymentId;
    private String paymentStatus;
    private String orderId;
    private Integer amount;
}
