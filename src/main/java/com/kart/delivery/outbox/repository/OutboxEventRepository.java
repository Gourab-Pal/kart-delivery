package com.kart.delivery.outbox.repository;

import com.kart.delivery.outbox.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, UUID> {

    @Query(value = """
        SELECT * FROM kart_delivery.outbox_event
        WHERE ((status = 'PENDING' AND next_attempt_at <= :now) OR (status = 'PROCESSING' AND locked_until <= :now))
        ORDER BY next_attempt_at ASC, created_at ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<OutboxEventEntity> findEventsToProcess(
            @Param("now")OffsetDateTime now,
            @Param("batchSize") int batchSize
    );
}