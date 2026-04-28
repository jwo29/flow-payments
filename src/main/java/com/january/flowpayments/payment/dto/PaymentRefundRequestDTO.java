package com.january.flowpayments.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentRefundRequestDTO {
    private Long paymentId;
    private BigDecimal amount;
    private String reason;
}
