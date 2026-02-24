package com.ericross.backend.outbox;

import com.ericross.backend.events.StoryStatusChangedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class OutboxPublisher {

    private final OutboxEventRepository repo;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<KafkaTemplate<String, StoryStatusChangedEvent>> kafkaTemplateProvider;

    @Scheduled(fixedDelayString = "${outbox.publisher.delay-ms:1000}")
    public void publishPending() {
        var kafkaTemplate = kafkaTemplateProvider.getIfAvailable();
        if (kafkaTemplate == null) return;

        var events = repo.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING);
        for (var e : events) {
            try {
                // We only publish StoryStatusChangedEvent for now
                StoryStatusChangedEvent event = objectMapper.readValue(e.getPayload(), StoryStatusChangedEvent.class);

                kafkaTemplate.send("story-status-events", e.getAggregateId().toString(), event);

                e.setStatus(OutboxStatus.SENT);
                e.setSentAt(Instant.now());
                repo.save(e);
            } catch (Exception ex) {
                e.setStatus(OutboxStatus.FAILED);
                repo.save(e);
            }
        }
    }
}
