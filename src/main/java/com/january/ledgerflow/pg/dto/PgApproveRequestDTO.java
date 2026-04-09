package com.january.ledgerflow.pg.dto;

import lombok.Getter;

@Getter
public class PgApproveRequestDTO {
    private String merchantId;
    private String orderId;
    private Long amount;
    private String cardNumber;
    private Integer installment;
}
