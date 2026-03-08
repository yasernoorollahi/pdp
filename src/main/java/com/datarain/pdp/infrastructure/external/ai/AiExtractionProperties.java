package com.datarain.pdp.infrastructure.external.ai;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "pdp.ai.extraction")
public class AiExtractionProperties {

    @NotBlank
    private String baseUrl = "http://localhost:3000";

    @NotBlank
    private String defaultProvider = "ollama";

    @NotBlank
    private String defaultModel = "qwen2.5:7b";

    private Duration connectTimeout = Duration.ofSeconds(5);

    private Duration readTimeout = Duration.ofMinutes(5);
}
