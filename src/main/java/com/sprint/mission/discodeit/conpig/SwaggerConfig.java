package com.sprint.mission.discodeit.conpig;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${swagger.server-url}")
    private String serverUrl;

    @Bean
    public OpenAPI customOpenApi(){
        List<Server> servers = List.of(
                new Server().url(serverUrl).description("로컬 개발 서버")
        );

        return new OpenAPI()
                .servers(servers)
                .info(apiInfo());
    }

    private Info apiInfo(){
        return new Info()
                .title("Spring Discodeit 로그인 환경 API")
                .description("Discodeit 로그인 및 채널, 메세지등의 기능을 제공하는 API 입니다.")
                .version("1.0.0");
    }
}
