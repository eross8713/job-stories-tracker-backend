package com.ericross.backend.dto;

import java.time.Instant;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

public record StoryResponse(
    @Schema(description = "Story id", example = "3fa85f64-5717-4562-b3fc-2c963f66afa6") UUID id,
    @Schema(description = "Short title for the story", example = "Improved reporting pipeline") String title,
    @Schema(description = "Situation / context", example = "Reporting was slow and manual") String situation,
    @Schema(description = "Task or goal", example = "Automate the ETL pipeline") String task,
    @Schema(description = "Actions taken", example = "Implemented batch jobs and monitoring") String action,
    @Schema(description = "Result / impact", example = "Reduced reporting time from 6h to 10m") String result,
    @Schema(description = "Comma-separated tags", example = "java,spring,postgres") String tags,
    @Schema(description = "Creation timestamp") Instant createdAt,
    @Schema(description = "Last update timestamp") Instant updatedAt
) {}
