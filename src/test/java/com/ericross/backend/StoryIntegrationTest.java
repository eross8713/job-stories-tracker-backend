package com.ericross.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.ericross.backend.dto.StoryResponse;
import com.ericross.backend.events.StoryStatusChangedEvent;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class StoryIntegrationTest {

    @SuppressWarnings("unused")
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("jobstories")
            .withUsername("jobstories")
            .withPassword("jobstories");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @SuppressWarnings("unused")
    @LocalServerPort
    private int port;

    @SuppressWarnings("unused")
    private final RestTemplate rest = new RestTemplate();

    @Test
    void createAndListStory() {
        String base = "http://localhost:" + port + "/api/stories";
        String json = "{\"title\":\"t\",\"situation\":\"s\",\"task\":\"t\",\"action\":\"a\",\"result\":\"r\",\"tags\":\"x\"}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> req = new HttpEntity<>(json, headers);

        ResponseEntity<String> createResp = rest.postForEntity(base, req, String.class);
        assertThat(createResp.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<StoryResponse[]> listResp = rest.getForEntity(base, StoryResponse[].class);
        assertThat(listResp.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(listResp.getBody()).isNotNull();
        assertThat(listResp.getBody().length).isGreaterThan(0);
    }

    @TestConfiguration
    static class TestKafkaConfig {
        @Bean
        @Primary
        public KafkaTemplate<String, StoryStatusChangedEvent> kafkaTemplate() {
            // Return a Mockito mock so autowiring succeeds during tests but we don't actually send messages.
            @SuppressWarnings("unchecked")
            KafkaTemplate<String, StoryStatusChangedEvent> mock = mock(KafkaTemplate.class);
            return mock;
        }
    }
}
