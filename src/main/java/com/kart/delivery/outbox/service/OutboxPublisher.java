package com.kart.delivery.outbox.service;

import com.kart.delivery.kafka.event.EventEnvelope;
import com.kart.delivery.outbox.entity.OutboxEventEntity;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OutboxPublisher {

    private static final Logger logger = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxClaimService outboxClaimService;
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private final String deliveryEventsTopic;

    public OutboxPublisher(
            OutboxClaimService outboxClaimService,
            KafkaTemplate<String, EventEnvelope> kafkaTemplate,
            @Value("${kafka.topic.delivery-events}") String deliveryEventsTopic
    ) {
        this.outboxClaimService = outboxClaimService;
        this.kafkaTemplate = kafkaTemplate;
        this.deliveryEventsTopic = deliveryEventsTopic;
    }

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {
        List<OutboxEventEntity> events = outboxClaimService.claimDueEvents();
        for (OutboxEventEntity outboxEvent : events) {
            publish(outboxEvent);
        }
    }

    private void publish(OutboxEventEntity outboxEvent) {
        try {
            EventEnvelope event = new EventEnvelope(
                    outboxEvent.getId(),
                    outboxEvent.getEventType(),
                    outboxEvent.getEventVersion(),
                    outboxEvent.getCreatedAt(),
                    outboxEvent.getPayload()
            );

            ProducerRecord<String, EventEnvelope> record = new ProducerRecord<>(
                    deliveryEventsTopic,
                    outboxEvent.getAggregateId().toString(),
                    event
            );

            record.headers().add(
                    "source-service",
                    "kart-delivery".getBytes(StandardCharsets.UTF_8)
            );

            kafkaTemplate.send(record).get(10, TimeUnit.SECONDS);

            outboxClaimService.markPublished(outboxEvent.getId());

            logger.info("Published outbox event {} of type {}", outboxEvent.getId(), outboxEvent.getEventType());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            recordFailure(outboxEvent, exception);
        } catch (Exception exception) {
            recordFailure(outboxEvent, exception);
        }
    }

    private void recordFailure(OutboxEventEntity outboxEvent, Exception exception) {
        String error = exception.getMessage();

        if (error == null || error.isBlank()) {
            error = exception.getClass().getSimpleName();
        }

        outboxClaimService.recordPublishFailure(
                outboxEvent.getId(),
                error
        );

        logger.warn("Failed to publish outbox event {}. Error: {}", outboxEvent.getId(), error);
    }
}