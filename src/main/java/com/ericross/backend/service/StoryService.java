package com.ericross.backend.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.ericross.backend.dto.StoryRequest;
import com.ericross.backend.dto.StoryResponse;
import com.ericross.backend.model.Story;
import com.ericross.backend.repository.StoryRepository;

@Service
public class StoryService {

    private final StoryRepository repo;

    public StoryService(StoryRepository repo) {
        this.repo = repo;
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
            s.getUpdatedAt()
        );
    }
}
