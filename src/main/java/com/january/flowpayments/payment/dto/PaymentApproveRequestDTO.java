package com.january.flowpayments.payment.dto;

import com.january.flowpayments.payment.domain.PaymentMethod;
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
