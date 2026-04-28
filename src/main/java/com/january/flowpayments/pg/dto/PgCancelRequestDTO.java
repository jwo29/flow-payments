package com.january.flowpayments.pg.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PgCancelRequestDTO {
    private String pgTransactionId;
    private BigDecimal amount;
    private String reason;
}
