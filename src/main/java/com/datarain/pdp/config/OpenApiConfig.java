package com.datarain.pdp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        tags = {
                @Tag(name = "Auth", description = "Authentication, token lifecycle, and logout operations."),
                @Tag(name = "Users", description = "User profiles, admin user management, and account state updates."),
                @Tag(name = "User Messages", description = "User message CRUD, processing status, and timeline queries."),
                @Tag(name = "Insights", description = "User insight timelines, trends, and summaries."),
                @Tag(name = "AI / Extraction", description = "AI-driven extraction and classification endpoints for messages."),
                @Tag(name = "AI / Signal Engine", description = "Admin controls and queries for AI signal processing."),
                @Tag(name = "AI / Normalization", description = "Admin controls for signal normalization pipeline."),
                @Tag(name = "Admin / Monitoring", description = "Admin system monitoring and operational overview."),
                @Tag(name = "Admin / Audit", description = "Admin access to security and business audit logs.")
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .name("Authorization")
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }
}
