package com.january.ledgerflow.pg.dto;

import lombok.Getter;

@Getter
public class PgApproveResponseDTO {
    private String pgTransactionId;
    private String orderId;
    private String status;
    private String approvedAt;
    private String authCode;
    private String message;
    private String errorCode;
}
