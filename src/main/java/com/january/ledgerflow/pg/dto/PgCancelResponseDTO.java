package com.january.ledgerflow.pg.dto;

import lombok.Getter;

@Getter
public class PgCancelResponseDTO {
    private String pgTransactionId;
    private String status;
    private String canceledAt;
    private String message;
    private String errorCode;
}
