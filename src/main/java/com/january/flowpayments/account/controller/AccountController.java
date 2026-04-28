package com.january.flowpayments.account.controller;

import com.january.flowpayments.account.dto.AccountCreateRequestDTO;
import com.january.flowpayments.account.service.AccountService;
import com.january.flowpayments.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ApiResponse<Long> createAccount(@RequestBody AccountCreateRequestDTO accountCreateRequestDTO) {
        Long accountId = accountService.createAccount(accountCreateRequestDTO);
        return ApiResponse.success(accountId);
    }

}
