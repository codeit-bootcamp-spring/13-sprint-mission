package com.sprint.mission.discodeit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;

@ConfigurationPropertiesScan //configurationProperties가 붙은 설정 클래스들을 자동으로 스캔
@SpringBootApplication
public class DiscodeitApplication { //spring boot 메인 애플리케이션 클래스

  public static void main(String[] args) {
    ConfigurableApplicationContext context = SpringApplication.run(DiscodeitApplication.class,
        args);
  }
}
