package com.ericross.backend.idempotency;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(name = "uq_idem_key_op", columnNames = {"idempotencyKey", "operation"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyRecord {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String operation; // e.g., "MARK_READY"

    @Column(nullable = false)
    private UUID resourceId; // storyId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdempotencyStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void init() {
        if (id == null) id = UUID.randomUUID();
        if (status == null) status = IdempotencyStatus.IN_PROGRESS;
        if (createdAt == null) createdAt = Instant.now();
    }
}
