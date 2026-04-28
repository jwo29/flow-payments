package com.january.flowpayments.account.dto;

import lombok.Getter;

@Getter
public class AccountCreateRequestDTO {

    private String accountNumber;
    private Long userId;

    public AccountCreateRequestDTO(String accountNumber, Long userId) {
        this.accountNumber = accountNumber;
        this.userId = userId;
    }
}
