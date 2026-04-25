package com.january.ledgerflow.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentTxId;

    private Long paymentId;
    private String type;
    private BigDecimal amount;
    private String status;
    private String pgResponse;

    private LocalDateTime createdAt;

}
