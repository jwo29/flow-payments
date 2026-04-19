package com.january.ledgerflow.payment.dto;

import lombok.Getter;

@Getter
public class PaymentRetryRequestDTO {
    private String cardNumber;
    private Integer installment;
}
