package com.january.ledgerflow.payment.dto;

import lombok.Getter;

@Getter
public class PaymentRequestDTO {
    private String orderId;
    private Integer amount;
    private String currency;
    private Long accountId;
}
