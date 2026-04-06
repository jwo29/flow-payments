package com.january.ledgerflow.payment.dto;

import lombok.Getter;

@Getter
public class PaymentAuthorizationResponseDTO {
    private Long paymentId;
    private String paymentStatus;
}
