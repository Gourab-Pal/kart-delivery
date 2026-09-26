package com.kart.delivery.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kart.delivery.outbox.entity.OutboxEventEntity;
import com.kart.delivery.outbox.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OutboxEventService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxEventService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveEvent(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            int eventVersion,
            Object payload
    ) {
        OutboxEventEntity outboxEvent =   new OutboxEventEntity(
                aggregateType,
                aggregateId,
                eventType,
                eventVersion,
                objectMapper.valueToTree(payload)
        );
        outboxEventRepository.save(outboxEvent);
    }
}