package com.january.flowpayments.pg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PgApproveRequestDTO {
    private String merchantId;
    private String orderId;
    private BigDecimal amount;
    private String cardNumber;
    private Integer installment;
}
