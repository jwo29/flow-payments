package com.january.flowpayments.payment.infrastructure;

import com.january.flowpayments.payment.service.CompensationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompensationRetryScheduler {

    private final CompensationService compensationService;

    @Scheduled(fixedDelay = 5000)
    public void retry() {
        compensationService.retryFailedCompensation();
    }
}
