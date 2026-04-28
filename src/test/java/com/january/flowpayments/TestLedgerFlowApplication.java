package com.january.flowpayments;

import org.springframework.boot.SpringApplication;

public class TestLedgerFlowApplication {

    public static void main(String[] args) {
        SpringApplication.from(FlowPaymentsApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
