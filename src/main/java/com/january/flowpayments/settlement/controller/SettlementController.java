package com.january.flowpayments.settlement.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settlements")
@Log4j2
@RequiredArgsConstructor
public class SettlementController {

    // todo 배치
    @PostMapping("/run")
    public void runSettlementDay() {
        /* 하루치 결제 정산
        * 하루 단위로 CAPTURED 거래 조회
        * settlement_flag = true 업데이트
        * settlement_result 테이블 생성
        * 배치 완료 시 SettlementCompletedEvent 발행
        * */
    }
}
