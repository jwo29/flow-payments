package com.january.ledgerflow.pg.dto;

import lombok.Getter;

@Getter
public class PgCancelRequestDTO {
    private String pgTransactionId;
    private Long amount;
    private String reason;
}
