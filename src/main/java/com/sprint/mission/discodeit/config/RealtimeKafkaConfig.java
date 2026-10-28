package com.sprint.mission.discodeit.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.*;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class RealtimeKafkaConfig {
  @Bean
  public NewTopic realtimeTopic() {
    return TopicBuilder.name("discodeit.RealtimeEvent").partitions(3).replicas(1).build();
  }
}
