package com.max.rest.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi publicUserApi() {
        return GroupedOpenApi.builder()
                .group("Users")
                .pathsToMatch("/api/users/**")
                .build();
    }

    @Bean
    public OpenAPI customOpenApi(@Value("${app.description}") String description,
                                 @Value("${app.version}") String version) {
        return
                new OpenAPI()
                        .info(new Info().title("Application API")
                                .version(version)
                                .description(description)
                                .contact(new Contact().name("Maksym Fylylpchuk").email("kavasak50@gmail.com")))
                        .servers(List.of(new Server().url("http://localhost:8080")
                                .description("Dev service")));
    }
}
