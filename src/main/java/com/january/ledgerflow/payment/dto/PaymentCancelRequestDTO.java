package com.january.ledgerflow.payment.dto;

import lombok.Getter;

@Getter
public class PaymentCancelRequestDTO {
    private Long paymentId;
    private String pgTransactionId;
    private String reason;
}
