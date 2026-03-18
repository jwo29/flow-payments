package com.january.ledgerflow.transaction.service;

import com.january.ledgerflow.account.domain.Account;
import com.january.ledgerflow.transaction.dto.DepositRequestDTO;
import com.january.ledgerflow.transaction.dto.WithdrawRequestDTO;
import com.january.ledgerflow.account.repository.AccountRepository;
import com.january.ledgerflow.transaction.domain.AccountLedger;
import com.january.ledgerflow.transaction.domain.Transaction;
import com.january.ledgerflow.transaction.repository.LedgerRepository;
import com.january.ledgerflow.transaction.repository.TransactionRepository;
import com.january.ledgerflow.transaction.vo.EntryType;
import com.january.ledgerflow.transaction.vo.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;

    @Transactional
    public void deposit(DepositRequestDTO depositRequestDTO) {
        Account account = accountRepository.findByIdForUpdate(depositRequestDTO.getAccountId())
                .orElseThrow(() -> new IllegalStateException("Account not found"));

        account.deposit(depositRequestDTO.getAmount());

        Transaction transaction = transactionRepository.save(
                new Transaction(
                        TransactionType.DEPOSIT,
                        depositRequestDTO.getAmount(),
                        null,
                        depositRequestDTO.getAccountId()));

        ledgerRepository.save(
                new AccountLedger(
                        depositRequestDTO.getAccountId(),
                        transaction.getTransactionId(),
                        EntryType.CREDIT,
                        depositRequestDTO.getAmount(),
                        account.getBalance())
        );
    }

    @Transactional
    public void withdraw(WithdrawRequestDTO withdrawRequestDTO) {
        Account account = accountRepository.findByIdForUpdate(withdrawRequestDTO.getAccountId())
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        account.withdraw(withdrawRequestDTO.getAmount());

        Transaction transaction = transactionRepository.save(
                new Transaction(TransactionType.WITHDRAW, withdrawRequestDTO.getAmount(), account.getAccountId(), null)
        );

        ledgerRepository.save(
                new AccountLedger(account.getAccountId(), transaction.getTransactionId(), EntryType.DEBIT, withdrawRequestDTO.getAmount(), account.getBalance())
        );
    }
}
