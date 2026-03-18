package com.january.ledgerflow.transaction.dto;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class TransferRequestDTO {
    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
}
