package org.shashanka.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.shashanka.domain.PaymentProcessedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class PaymentEventProducer {
    private final KafkaTemplate<String, PaymentProcessedEvent> kafkaTemplate;

    public void publishEvent(final PaymentProcessedEvent paymentProcessedEvent) {

        kafkaTemplate.send("processed-payments.v0", String.valueOf(paymentProcessedEvent.accountId()), paymentProcessedEvent);
    }
}
