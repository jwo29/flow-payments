package com.january.flowpayments.settlement.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SettlementResultDTO {
    private Integer settlementCount;
    private LocalDateTime batchTime;
}
