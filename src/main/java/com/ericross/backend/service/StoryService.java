package com.ericross.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.ericross.backend.idempotency.*;
import com.ericross.backend.outbox.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.ericross.backend.events.StoryStatusChangedEvent;
import com.ericross.backend.model.StoryStatus;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ericross.backend.dto.StoryRequest;
import com.ericross.backend.dto.StoryResponse;
import com.ericross.backend.model.Story;
import com.ericross.backend.repository.StoryRepository;

@Service
public class StoryService {

    private final StoryRepository repo;
    private final KafkaTemplate<String, StoryStatusChangedEvent> kafkaTemplate;
    private final IdempotencyRecordRepository idemRepo;
    private final OutboxEventRepository outboxRepo;
    private final ObjectMapper objectMapper;

    public StoryService(
            StoryRepository repo,
            ObjectProvider<KafkaTemplate<String,
                    StoryStatusChangedEvent>> kafkaTemplateProvider,
            IdempotencyRecordRepository idemRepo,
            OutboxEventRepository outboxRepo,
            ObjectMapper objectMapper) {
        this.idemRepo = idemRepo;
        this.objectMapper = objectMapper;
        this.outboxRepo = outboxRepo;
        this.repo = repo;
        // kafkaTemplate may be absent in environments where Kafka is not configured.
        this.kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
    }

    public StoryResponse create(StoryRequest req) {
        Story s = Story.builder()
            .title(req.title())
            .situation(req.situation())
            .task(req.task())
            .action(req.action())
            .result(req.result())
            .tags(req.tags())
            .build();
        Story saved = repo.save(s);
        return toDto(saved);
    }

    public List<StoryResponse> list() {
        return repo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public StoryResponse get(UUID id) {
        Story s = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found"));
        return toDto(s);
    }

    private StoryResponse toDto(Story s) {
        return new StoryResponse(
            s.getId(),
            s.getTitle(),
            s.getSituation(),
            s.getTask(),
            s.getAction(),
            s.getResult(),
            s.getTags(),
            s.getCreatedAt(),
            s.getUpdatedAt(),
            s.getStatus()
        );
    }

    @Transactional
    public StoryResponse markReady(UUID id, String idempotencyKey) {

        // 1) Idempotency check
        var existing = idemRepo.findByIdempotencyKeyAndOperation(idempotencyKey, "MARK_READY");
        if (existing.isPresent() && existing.get().getStatus() == IdempotencyStatus.SUCCESS) {
            // Safe: return current story state (idempotent replay)
            return get(id);
        }

        // Create/lock idempotency record
        if (existing.isEmpty()) {
            idemRepo.save(IdempotencyRecord.builder()
                    .idempotencyKey(idempotencyKey)
                    .operation("MARK_READY")
                    .resourceId(id)
                    .status(IdempotencyStatus.IN_PROGRESS)
                    .build());
        }

        // 2) Update story
        Story s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found"));

        s.setStatus(StoryStatus.READY);
        Story saved = repo.save(s);

        // 3) Write outbox event (same transaction)
        String payload;
        try {
            payload = objectMapper.writeValueAsString(new StoryStatusChangedEvent(
                    saved.getId(),
                    saved.getStatus().name(),
                    Instant.now()
            ));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize event");
        }

        String dedupeKey = "STORY:" + saved.getId() + ":STATUS:READY:" + idempotencyKey;

        outboxRepo.save(OutboxEvent.builder()
                .aggregateType("STORY")
                .aggregateId(saved.getId())
                .eventType("STORY_STATUS_CHANGED")
                .payload(payload)
                .dedupeKey(dedupeKey)
                .status(OutboxStatus.PENDING)
                .build());

        // 4) Mark idempotency success
        var record = idemRepo.findByIdempotencyKeyAndOperation(idempotencyKey, "MARK_READY").orElseThrow();
        record.setStatus(IdempotencyStatus.SUCCESS);
        idemRepo.save(record);

        return toDto(saved);
    }


}
