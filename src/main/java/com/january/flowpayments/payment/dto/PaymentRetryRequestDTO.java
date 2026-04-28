package com.january.flowpayments.payment.dto;

import lombok.Getter;

@Getter
public class PaymentRetryRequestDTO {
    private String cardNumber;
    private Integer installment;
}
