package com.january.ledgerflow.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DepositRequestDTO {
    private Long accountId;
    private BigDecimal amount;
}
