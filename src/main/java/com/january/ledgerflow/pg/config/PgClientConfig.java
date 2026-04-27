package com.january.ledgerflow.pg.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;

@Configuration
public class PgClientConfig {

    @Bean
    public RestClient pgRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(1));
        requestFactory.setReadTimeout(Duration.ofSeconds(2));

        return RestClient.builder()
                .baseUrl("http://localhost:8081/pg/v1") // Mock PG 서버 주소
                .requestFactory(requestFactory)
                .defaultStatusHandler(statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError()
                        , ((request, response) -> {
                            if (response.getStatusCode().is4xxClientError()) {
                                throw new RuntimeException("Client exception");
                            }
                            if (response.getStatusCode().is5xxServerError()) {
                                throw new RuntimeException("Server exception");
                            }
                            throw new RestClientException("Unexpected response status: " + response.getStatusCode());
                        })
                )
                .build();
    }
}
