package com.january.flowpayments.messaging.producer;

import com.january.flowpayments.messaging.config.RabbitMQConfig;
import com.january.flowpayments.messaging.dto.TransactionEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@Log4j2
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publish(TransactionEventDTO transactionEventDTO) {

        log.info("Publishing transaction event to RabbitMQ");
        log.info("message: {}", transactionEventDTO);

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.ROUTING_KEY,
                transactionEventDTO
        );
    }
}
