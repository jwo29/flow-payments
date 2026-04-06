package com.january.ledgerflow.payment.dto;

import lombok.Getter;

@Getter
public class PaymentInformationResponseDTO {
    private Long paymentId;
    private String orderId;
    private Integer amount;
    private String paymentStatus;
//    private List<> history;
}
