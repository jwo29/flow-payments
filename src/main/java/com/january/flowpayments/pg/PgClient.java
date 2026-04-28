package com.january.flowpayments.pg;

import com.january.flowpayments.pg.dto.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PgClient {

    private final RestClient restClient;
    private final String pgApiKey = "pg_test_key_123"; // 설정 파일로 분리 가능

    public PgClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public PgApproveResponseDTO approve(PgApproveRequestDTO pgApproveRequestDTO) {
        return restClient.post()
                .uri("/payments/approve")
                .header("X-PG-API-KEY", pgApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(pgApproveRequestDTO)
                .retrieve()
                .body(PgApproveResponseDTO.class);
    }

    public PgCancelResponseDTO cancel(PgCancelRequestDTO cancelRequestDTO) {
        return restClient.post()
                .uri("/payments/cancel")
                .header("X-PG-API-KEY", pgApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(cancelRequestDTO)
                .retrieve()
                .body(PgCancelResponseDTO.class);
    }

    public PgInquiryResponseDTO inquiry(String pgTransactionId) {
        return restClient.get()
                .uri("/payments/inquiry/" + pgTransactionId)
                .header("X-PG-API-KEY", pgApiKey)
                .retrieve()
                .body(PgInquiryResponseDTO.class);
    }
}
