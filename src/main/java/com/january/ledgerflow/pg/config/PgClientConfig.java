package com.january.ledgerflow.pg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PgClientConfig {

    @Bean
    public RestClient pgRestClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:8081/pg/v1") // Mock PG 서버 주소
                .build();
    }
}
