package com.january.ledgerflow.messaging.consumer;

import com.january.ledgerflow.messaging.config.RabbitMQConfig;
import com.january.ledgerflow.messaging.dto.TransactionEventDTO;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/*
DLQ는 "실패한 메시지의 보관소"가 아니라 **운영 대응 지점**이다.
반드시 포함해야 할 로직
1) 원인 로깅
    - 어떤 메시지인지
    - 왜 실패했는지
    - 몇 번 실패했는지
2) 재처리 가능 여부 판단
    - 비즈니스 오류 → 재처리 불가
    - 일시적 오류 → 재처리 대상
3) 재처리 또는 영구 보관
    - 기본: DB 저장 + 알림(Slack/Email)
    - 고급: 재발행(Retry Queue or 원래 큐)
 */
@Component
@Log4j2
@RequiredArgsConstructor
public class DLQEventListener {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.DLQ)
    public void consume(Message message, Channel channel) throws Exception {

        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            TransactionEventDTO transactionEventDTO = objectMapper.readValue(
                    message.getBody(),
                    TransactionEventDTO.class);

            log.info("DLQ 수신: {}", transactionEventDTO);

            // 1. DB 저장 or 알림
            saveFailure(transactionEventDTO);

            // 2. ACK (DLQ에서는 반드시 ACK)
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("DLQ 처리 실패: {}", e.getMessage());

            // DLQ에서 실패하면 무한루프 방지를 위해 ACK
            channel.basicAck(deliveryTag, false);
        }
    }

    private void saveFailure(TransactionEventDTO transactionEventDTO) {
        // todo dead_letter_logs 테이블에 저장
    }
}
