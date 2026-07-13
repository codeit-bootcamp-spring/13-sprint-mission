package com.sprint.mission.discodeit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI discodeitOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Discodeit API")
                        .description("Discodeit REST API documentation")
                        .version("v1"))
                .servers(List.of(new Server()
                        .url("http://localhost:8080")
                        .description("Local server")));
    }
}
