package com.january.ledgerflow.payment.controller;

import com.january.ledgerflow.common.response.ApiResponse;
import com.january.ledgerflow.payment.domain.Payment;
import lombok.Getter;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @PostMapping("")
    public ApiResponse<Long> createPayment() {
        return ApiResponse.success(0L);
    }

    @PostMapping("/{id}/authorize")
    public void authorizePaymentById(@PathVariable("id") Long id) {

    }

    @PostMapping("/{id}/capture")
    public void capturePayment(@PathVariable("id") Long id) {

    }

    @PostMapping("/{id}/cancel")
    public void cancelPayment(@PathVariable("id") Long id) {

    }

    @PostMapping("/{id}/refund")
    public void refundPayment(@PathVariable("id") Long id) {

    }

    @GetMapping("/{id}")
    public ApiResponse<Payment> getPaymentInformation(@PathVariable("id") Long id) {
        return ApiResponse.success(new Payment());
    }

}
