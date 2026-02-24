package com.ericross.backend.outbox;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "outbox_events",
        uniqueConstraints = @UniqueConstraint(name = "uq_outbox_dedupe_key", columnNames = {"dedupeKey"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String aggregateType; // e.g., "STORY"

    @Column(nullable = false)
    private UUID aggregateId; // storyId

    @Column(nullable = false)
    private String eventType; // e.g., "STORY_STATUS_CHANGED"

    @Column(nullable = false, columnDefinition = "text")
    private String payload; // JSON string

    @Column(nullable = false)
    private String dedupeKey; // e.g., "STORY:{id}:STATUS:READY[:idemKey]"

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutboxStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant sentAt;

    @PrePersist
    void init() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = OutboxStatus.PENDING;
        if (createdAt == null) createdAt = Instant.now();
    }
}
