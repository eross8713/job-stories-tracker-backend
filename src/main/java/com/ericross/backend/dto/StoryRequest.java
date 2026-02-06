package com.ericross.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record StoryRequest(
    @Schema(description = "Short title for the story", example = "Improved reporting pipeline")
    @NotBlank @Size(max = 255) String title,

    @Schema(description = "Situation / context", example = "Reporting was slow and manual")
    @NotBlank String situation,

    @Schema(description = "Task or goal", example = "Automate the ETL pipeline")
    @NotBlank String task,

    @Schema(description = "Actions taken", example = "Implemented batch jobs and monitoring")
    @NotBlank String action,

    @Schema(description = "Result / impact", example = "Reduced reporting time from 6h to 10m")
    @NotBlank String result,

    @Schema(description = "Comma-separated tags", example = "java,spring,postgres")
    String tags
) {}
