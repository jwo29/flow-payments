package com.january.flowpayments.payment.controller;

import com.january.flowpayments.common.response.ApiResponse;
import com.january.flowpayments.payment.dto.PaymentApproveRequestDTO;
import com.january.flowpayments.payment.dto.PaymentRefundRequestDTO;
import com.january.flowpayments.payment.dto.PaymentResponseDTO;
import com.january.flowpayments.payment.dto.PaymentRetryRequestDTO;
import com.january.flowpayments.payment.service.PaymentService;
import com.january.flowpayments.payment.service.PaymentTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentTransactionService paymentTransactionService;

    /**
     * 결제 생성 + 승인 + 매입 자동 처리
     * @return
     */
    @PostMapping("")
    public ApiResponse<PaymentResponseDTO> createPayment(@RequestBody PaymentApproveRequestDTO paymentApproveRequestDTO) {
        PaymentResponseDTO paymentResponseDTO = paymentService.approve(paymentApproveRequestDTO);
        return ApiResponse.success(paymentResponseDTO);
    }

    /**
     * 승인 취소
     * @param id
     */
    @PostMapping("/{id}/cancel")
    public ApiResponse<PaymentResponseDTO> cancelPayment(@PathVariable("id") Long id, @RequestBody PaymentRefundRequestDTO paymentRefundRequestDTO) {
        PaymentResponseDTO paymentResponseDTO = paymentService.cancel(paymentRefundRequestDTO);
        return ApiResponse.success(paymentResponseDTO);
    }

    /**
     * 환불
     * @param id
     */
    @PostMapping("/{id}/refund")
    public ApiResponse<PaymentResponseDTO> refundPayment(@PathVariable("id") Long id, @RequestBody PaymentRefundRequestDTO paymentRefundRequestDTO) {
        PaymentResponseDTO paymentResponseDTO = paymentService.refund(paymentRefundRequestDTO);
        return ApiResponse.success(paymentResponseDTO);
    }

    @PostMapping("/{id}/retry")
    public ApiResponse<PaymentResponseDTO> retryPayment(@PathVariable("id") Long paymentId,
                                                        @RequestBody PaymentRetryRequestDTO paymentRetryRequestDTO) {
        return ApiResponse.success(paymentService.retry(paymentId, paymentRetryRequestDTO));
    }

}
