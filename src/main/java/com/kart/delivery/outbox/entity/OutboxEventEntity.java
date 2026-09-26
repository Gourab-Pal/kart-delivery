package com.kart.delivery.outbox.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_event", schema = "kart_order")
public class OutboxEventEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "aggregate_type", nullable = false, length = 100)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false)
    private UUID aggregateId;

    @Column(name = "event_type", nullable = false, length = 150)
    private String eventType;

    @Column(name = "event_version", nullable = false)
    private int eventVersion;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private JsonNode payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "attempt_count", nullable = false)
    private int attemptCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "next_attempt_at", nullable = false)
    private OffsetDateTime nextAttemptAt;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "locked_until")
    private OffsetDateTime lockedUntil;

    @Column(name = "last_error")
    private String lastError;

    protected OutboxEventEntity() {
    }

    public OutboxEventEntity(
            String aggregateType,
            UUID aggregateId,
            String eventType,
            int eventVersion,
            JsonNode payload
    ) {
        if (eventVersion <= 0) {
            throw new IllegalArgumentException("Event version must be greater than zero");
        }

        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.eventVersion = eventVersion;
        this.payload = payload;
        this.status = Status.PENDING;
        this.attemptCount = 0;

        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.nextAttemptAt = now;
    }

    public void markProcessing(OffsetDateTime lockedUntil) {
        this.status = Status.PROCESSING;
        this.attemptCount++;
        this.lockedUntil = lockedUntil;
    }

    public void markPublished() {
        this.status = Status.PUBLISHED;
        this.publishedAt = OffsetDateTime.now();
        this.lockedUntil = null;
        this.lastError = null;
    }

    public void scheduleRetry(OffsetDateTime nextAttemptAt, String error) {
        this.status = Status.PENDING;
        this.nextAttemptAt = nextAttemptAt;
        this.lockedUntil = null;
        this.lastError = error;
    }

    public void markFailed(String error) {
        this.status = Status.FAILED;
        this.lockedUntil = null;
        this.lastError = error;
    }

    public UUID getId() {
        return id;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public UUID getAggregateId() {
        return aggregateId;
    }

    public String getEventType() {
        return eventType;
    }

    public int getEventVersion() {
        return eventVersion;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public Status getStatus() {
        return status;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getNextAttemptAt() {
        return nextAttemptAt;
    }

    public OffsetDateTime getPublishedAt() {
        return publishedAt;
    }

    public OffsetDateTime getLockedUntil() {
        return lockedUntil;
    }

    public String getLastError() {
        return lastError;
    }

    public enum Status {
        PENDING,
        PROCESSING,
        PUBLISHED,
        FAILED
    }
}