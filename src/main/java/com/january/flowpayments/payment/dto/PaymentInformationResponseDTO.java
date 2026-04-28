package com.january.flowpayments.payment.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class PaymentInformationResponseDTO {
    private Long paymentId;
    private String orderId;
    private BigDecimal amount;
    private String paymentStatus;
//    private List<> history;
}
