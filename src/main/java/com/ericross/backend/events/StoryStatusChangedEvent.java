package com.ericross.backend.events;


import java.util.UUID;
import java.time.Instant;

public record StoryStatusChangedEvent(
        UUID storyId,
        String newStatus,
        Instant occurredAt
) {}
