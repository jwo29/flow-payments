package com.january.ledgerflow.transaction.repository;

import com.january.ledgerflow.transaction.domain.AccountLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerRepository extends JpaRepository<AccountLedger, Long> {

    List<AccountLedger> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
