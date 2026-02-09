package com.ericross.backend.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public StoryService(
            StoryRepository repo,
            ObjectProvider<KafkaTemplate<String, StoryStatusChangedEvent>> kafkaTemplateProvider) {
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

    public StoryResponse markReady(UUID id) {
        Story s = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Story not found"));

        s.setStatus(StoryStatus.READY);
        Story saved = repo.save(s);

        if (this.kafkaTemplate != null) {
            kafkaTemplate.send(
                    "story-status-events",
                    saved.getId().toString(),
                    new StoryStatusChangedEvent(
                            saved.getId(),
                            saved.getStatus().name(),
                            Instant.now()
                    )
            );
        }

        return toDto(saved);
    }

}
