package com.january.ledgerflow.payment.controller;

import com.january.ledgerflow.common.response.ApiResponse;
import com.january.ledgerflow.payment.domain.Payment;
import com.january.ledgerflow.payment.dto.*;
import com.january.ledgerflow.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 결제 생성 + 승인 + 매입 자동 처리
     * @return
     */
    @PostMapping("")
    public ApiResponse<PaymentApproveResponseDTO> createPayment(@RequestBody PaymentApproveRequestDTO paymentApproveRequestDTO) {
        PaymentApproveResponseDTO paymentApproveResponseDTO = paymentService.approve(paymentApproveRequestDTO);
        return ApiResponse.success(paymentApproveResponseDTO);
    }

    /**
     * 승인 취소
     * @param id
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<PaymentCancelResponseDTO> cancelPayment(@PathVariable("id") Long id, @RequestBody PaymentCancelRequestDTO paymentCancelRequestDTO) {
        PaymentCancelResponseDTO paymentCancelResponseDTO = paymentService.cancel(paymentCancelRequestDTO);
        return ApiResponse.success(paymentCancelResponseDTO);
    }

    /**
     * 환불
     * @param id
     */
    @PostMapping("/{id}/refund")
    public ApiResponse<PaymentRefundResponseDTO> refundPayment(@PathVariable("id") Long id, @RequestBody PaymentRefundRequestDTO paymentRefundRequestDTO) {
        PaymentRefundResponseDTO paymentRefundResponseDTO = paymentService.refund(paymentRefundRequestDTO);
        return ApiResponse.success(paymentRefundResponseDTO);
    }

}
