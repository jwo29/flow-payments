package com.january.ledgerflow.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentCancelResponseDTO {
    private Long paymentId;
    private String paymentStatus;
    private String orderId;
    private BigDecimal amount;
}
