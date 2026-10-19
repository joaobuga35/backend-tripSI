package com.tripsi.api.shared.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void should_expose_bearer_jwt_security_scheme_required_globally() {
        OpenAPI openApi = config.tripSiOpenApi();

        assertThat(openApi.getSecurity())
                .anySatisfy(requirement -> assertThat(requirement).containsKey(OpenApiConfig.BEARER_SCHEME_NAME));

        SecurityScheme scheme = openApi.getComponents().getSecuritySchemes().get(OpenApiConfig.BEARER_SCHEME_NAME);
        assertThat(scheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(scheme.getScheme()).isEqualTo("bearer");
        assertThat(scheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void should_set_api_title_and_version() {
        OpenAPI openApi = config.tripSiOpenApi();

        assertThat(openApi.getInfo().getTitle()).isEqualTo("TripSi API");
        assertThat(openApi.getInfo().getVersion()).isEqualTo("v1");
    }
}
