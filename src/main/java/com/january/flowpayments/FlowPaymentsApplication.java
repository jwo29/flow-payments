package com.january.flowpayments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FlowPaymentsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowPaymentsApplication.class, args);
    }

}
