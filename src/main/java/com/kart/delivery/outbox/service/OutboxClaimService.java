package com.kart.delivery.outbox.service;

import com.kart.delivery.outbox.entity.OutboxEventEntity;
import com.kart.delivery.outbox.repository.OutboxEventRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxClaimService {

    private static final int BATCH_SIZE = 1;
    private static final long LOCK_DURATION_SECONDS = 60;
    private static final int MAX_ATTEMPTS = 5;
    private static final long MAX_RETRY_DELAY_SECONDS = 60;

    private final OutboxEventRepository outboxEventRepository;

    public OutboxClaimService(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    @Transactional
    public List<OutboxEventEntity> claimDueEvents() {
        OffsetDateTime now = OffsetDateTime.now();

        List<OutboxEventEntity> eligibleEvents = outboxEventRepository.findEventsToProcess(now, BATCH_SIZE);

        List<OutboxEventEntity> claimedEvents = new ArrayList<>();
        OffsetDateTime lockedUntil = now.plusSeconds(LOCK_DURATION_SECONDS);

        for (OutboxEventEntity event : eligibleEvents) {
            if (event.getAttemptCount() >= MAX_ATTEMPTS) {
                event.markFailed("Maximum publish attempts reached before the event was published");
                continue;
            }

            event.markProcessing(lockedUntil);
            claimedEvents.add(event);
        }

        return claimedEvents;
    }

    @Transactional
    public void markPublished(UUID eventId) {
        OutboxEventEntity event = outboxEventRepository.findById(eventId).orElseThrow(() -> new IllegalStateException("Outbox event not found: " + eventId));
        event.markPublished();
    }

    @Transactional
    public void recordPublishFailure(UUID eventId, String error) {
        OutboxEventEntity event = outboxEventRepository.findById(eventId).orElseThrow(() -> new IllegalStateException("Outbox event not found: " + eventId));

        if (event.getAttemptCount() >= MAX_ATTEMPTS) {
            event.markFailed(error);
            return;
        }

        long retryDelaySeconds = calculateRetryDelaySeconds(event.getAttemptCount());
        OffsetDateTime nextAttemptAt = OffsetDateTime.now().plusSeconds(retryDelaySeconds);
        event.scheduleRetry(nextAttemptAt, error);
    }

    private long calculateRetryDelaySeconds(int attemptCount) {
        long delaySeconds = 1;

        for (int retryNumber = 1; retryNumber < attemptCount; retryNumber++) {
            delaySeconds = Math.min(delaySeconds * 2, MAX_RETRY_DELAY_SECONDS);
        }

        return delaySeconds;
    }
}