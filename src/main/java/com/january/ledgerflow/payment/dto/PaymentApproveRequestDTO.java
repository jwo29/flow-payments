package com.january.ledgerflow.payment.dto;

import com.january.ledgerflow.payment.vo.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PaymentApproveRequestDTO {
    private String merchantId;
    private Long userId;
    private Long accountId;
    private Long merchantAccountId;

    private BigDecimal amount;
    private String orderId;

    private String cardNumber;
    private Integer installment;

    private PaymentMethod paymentMethod;
}
