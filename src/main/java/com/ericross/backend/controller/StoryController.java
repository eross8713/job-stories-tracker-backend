package com.ericross.backend.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import com.ericross.backend.dto.StoryRequest;
import com.ericross.backend.dto.StoryResponse;
import com.ericross.backend.service.StoryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@Tag(name = "Stories", description = "APIs to create and retrieve job stories")
@RestController
@RequestMapping("/api/stories")
public class StoryController {

    private final StoryService svc;

    public StoryController(StoryService svc) {
        this.svc = svc;
    }

    @Operation(summary = "Create a story")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Story created"),
        @ApiResponse(responseCode = "500", description = "Invalid request")
    })
    @PostMapping
    public ResponseEntity<StoryResponse> create(@Valid @RequestBody StoryRequest req) {
        StoryResponse created = svc.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "List stories")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of stories")
    })
    @GetMapping
    public List<StoryResponse> list() {
        return svc.list();
    }

    @Operation(summary = "Get a story by id")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Story found"),
        @ApiResponse(responseCode = "404", description = "Story not found")
    })
    @GetMapping("/{id}")
    public StoryResponse get(@PathVariable UUID id) {
        return svc.get(id);
    }

    @PostMapping("/{id}/ready")
    public StoryResponse markReady(@PathVariable UUID id) {
        return svc.markReady(id);
    }

}
