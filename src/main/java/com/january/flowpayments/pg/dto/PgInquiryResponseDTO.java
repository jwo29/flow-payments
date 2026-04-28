package com.january.flowpayments.pg.dto;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PgInquiryResponseDTO {
    private String status;
    private String pgTransactionId;
    private String authCode;
}
