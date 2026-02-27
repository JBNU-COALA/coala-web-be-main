package com.example.coalawebbackend.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;

@OpenAPIDefinition(
        servers = {
                @Server(url = "http://localhost:8080", description = "로컬 서버")
        }
)
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info =
                new Info()
                        .title("Coala Web Backend API")
                        .version("v1.0.0")
                        .description("Coala Community Project");

        String accessKey = "Access Token (Bearer)";
        String refreshKey = "Refresh Token";

        SecurityRequirement securityRequirement =
                new SecurityRequirement().addList(accessKey).addList(refreshKey);

        SecurityScheme accessTokenSecurityScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name(HttpHeaders.AUTHORIZATION);

        SecurityScheme refreshTokenSecurityScheme =
                new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-Refresh-Token");

        Components components =
                new Components()
                        .addSecuritySchemes(accessKey, accessTokenSecurityScheme)
                        .addSecuritySchemes(refreshKey, refreshTokenSecurityScheme);

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}
