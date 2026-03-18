package com.january.ledgerflow.transaction.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class WithdrawRequestDTO {
    private Long accountId;
    private BigDecimal amount;
}
