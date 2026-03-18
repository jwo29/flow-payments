package com.january.ledgerflow.transaction.controller;

import com.january.ledgerflow.transaction.dto.DepositRequestDTO;
import com.january.ledgerflow.transaction.dto.TransferRequestDTO;
import com.january.ledgerflow.transaction.dto.WithdrawRequestDTO;
import com.january.ledgerflow.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private TransactionService transactionService;

    @PostMapping("/{id}/deposit")
    public void deposit(@RequestBody DepositRequestDTO depositRequestDTO, @PathVariable Long id) {
        transactionService.deposit(depositRequestDTO);
    }

    @PostMapping("/{id}/withdraw")
    public void withdraw(@RequestBody WithdrawRequestDTO withdrawRequestDTO, @PathVariable Long id) {
        transactionService.withdraw(withdrawRequestDTO);
    }

    @PostMapping("/transfer")
    public void transfer(@RequestBody TransferRequestDTO transferRequestDTO) {

    }

}
