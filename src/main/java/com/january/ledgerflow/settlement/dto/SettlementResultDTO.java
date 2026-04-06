package com.january.ledgerflow.settlement.dto;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class SettlementResultDTO {
    private Integer settlementCount;
    private LocalDateTime batchTime;
}
