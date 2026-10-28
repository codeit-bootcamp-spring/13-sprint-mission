package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.kafka.listener.MessageListenerContainer;

class KafkaConfigTest {

  @Test
  void deserializationFailureIsRecoveredWithoutRetryEvenWhenWrappedByListener() {
    DefaultErrorHandler handler = new KafkaConfig().kafkaErrorHandler();
    var exception = new ListenerExecutionFailedException("listener failed",
        new IllegalArgumentException("invalid JSON"));

    boolean recovered = handler.handleOne(exception,
        new ConsumerRecord<>("discodeit.MessageCreatedEvent", 0, 0L, "key", "invalid JSON"),
        mock(Consumer.class), mock(MessageListenerContainer.class));

    assertThat(recovered).isTrue();
  }

  @Test
  void transientFailureRemainsRetryable() {
    DefaultErrorHandler handler = new KafkaConfig().kafkaErrorHandler();

    boolean recovered = handler.handleOne(new IllegalStateException("temporary failure"),
        new ConsumerRecord<>("discodeit.MessageCreatedEvent", 0, 0L, "key", "{}"),
        mock(Consumer.class), mock(MessageListenerContainer.class));

    assertThat(recovered).isFalse();
  }
}
