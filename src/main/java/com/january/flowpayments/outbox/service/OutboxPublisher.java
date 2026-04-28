package com.january.flowpayments.outbox.service;

import com.january.flowpayments.messaging.config.RabbitMQConfig;
import com.january.flowpayments.messaging.dto.TransactionEventDTO;
import com.january.flowpayments.outbox.domain.OutboxEvent;
import com.january.flowpayments.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final ObjectMapper objectMapper;

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publish() {

        List<OutboxEvent> events = outboxEventRepository.findTop100ByStatus("PENDING");

        for (OutboxEvent event : events) {

            TransactionEventDTO transactionEventDTO = objectMapper.readValue(event.getPayload(), TransactionEventDTO.class);

            try {
                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.EXCHANGE_NAME,
                        RabbitMQConfig.ROUTING_KEY,
                        transactionEventDTO
                );

                event.markSent();

            } catch (Exception e) {
                event.markFailed();
            }
        }
    }
}
