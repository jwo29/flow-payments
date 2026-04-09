package com.january.ledgerflow.pg;

import com.january.ledgerflow.pg.dto.PgApproveResponseDTO;
import com.january.ledgerflow.pg.dto.PgApproveRequestDTO;
import com.january.ledgerflow.pg.dto.PgCancelRequestDTO;
import com.january.ledgerflow.pg.dto.PgCancelResponseDTO;
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
}
