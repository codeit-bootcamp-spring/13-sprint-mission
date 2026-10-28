package com.sprint.mission.discodeit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;

@Configuration
public class KafkaConfig {

  @Bean
  public DefaultErrorHandler kafkaErrorHandler() {
    DefaultErrorHandler errorHandler = new DefaultErrorHandler();
    // JSON 파싱 실패는 같은 메시지를 다시 처리해도 복구되지 않는다.
    errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
    return errorHandler;
  }
}
