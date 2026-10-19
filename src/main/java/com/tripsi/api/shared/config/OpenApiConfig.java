package com.tripsi.api.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Defines Swagger UI metadata and registers the global Bearer JWT security scheme.
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI tripSiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("TripSi API")
                        .version("v1")
                        .description("AI-driven travel planning API — modular monolith with hexagonal architecture."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME_NAME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
