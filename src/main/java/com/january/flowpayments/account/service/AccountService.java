package com.january.flowpayments.account.service;

import com.january.flowpayments.account.domain.Account;
import com.january.flowpayments.account.dto.AccountCreateRequestDTO;
import com.january.flowpayments.account.repository.AccountRepository;
import com.january.flowpayments.common.exception.CustomException;
import com.january.flowpayments.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public Long createAccount(@RequestBody AccountCreateRequestDTO accountCreateRequestDTO) {
        accountRepository.findByAccountNumber(accountCreateRequestDTO.getAccountNumber())
                .ifPresent(account -> {
                    throw new CustomException(ErrorCode.ACCOUNT_ALREADY_EXISTS);
                });

        Account account = new Account(
                accountCreateRequestDTO.getAccountNumber(),
                accountCreateRequestDTO.getUserId()
        );

        Account savedAccount = accountRepository.save(account);

        return savedAccount.getAccountId();
    }

}
