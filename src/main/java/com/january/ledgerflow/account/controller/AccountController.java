package com.january.ledgerflow.account.controller;

import com.january.ledgerflow.account.dto.AccountCreateRequestDTO;
import com.january.ledgerflow.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public Long createAccount(@RequestBody AccountCreateRequestDTO accountCreateRequestDTO) {
        return accountService.createAccount(accountCreateRequestDTO);
    }

}
