package com.sprint.mission.discodeit.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig { // 전체 설정 잡는 클래스

  @Bean
  public OpenAPI customOpenAPI() {
    List<Server> servers = List.of(
        new Server().url("http://localhost:8080").description("로컬 서버"));
    return new OpenAPI().servers(servers).info(apiInfo());
  }

  private Info apiInfo() {
    return new Info().title("Discodeit API 문서")
        .description("Discodeit 프로젝트의 Swagger API 문서입니다.")
        .version("1.0.0")
        .license(new License().name("MIT License").url("https://opensource.org/licenses/MIT"));
  }
}
