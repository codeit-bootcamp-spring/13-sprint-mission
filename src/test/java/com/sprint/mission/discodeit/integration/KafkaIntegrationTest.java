
package com.sprint.mission.discodeit.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.event.RoleUpdatedEvent;
import com.sprint.mission.discodeit.event.SseEventListener;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class KafkaIntegrationTest {

    private static final String TOPIC = "discodeit.integration.events";

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @MockitoBean
    private SseEventListener sseEventListener;

    @Test
    @Timeout(45)
    @DisplayName("실제 Kafka 브로커에 이벤트를 발행하면 Consumer가 수신한다")
    void kafka_publish_and_consume_success() throws Exception {
        // given
        createTestTopic();

        UUID userId = UUID.randomUUID();

        RoleUpdatedEvent event = new RoleUpdatedEvent(
                userId,
                Role.USER,
                Role.CHANNEL_MANAGER
        );

        String payload = objectMapper.writeValueAsString(event);

        Properties properties = new Properties();
        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );
        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "discodeit-integration-" + UUID.randomUUID()
        );
        properties.put(
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        properties.put(
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class
        );
        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "latest"
        );
        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        try (KafkaConsumer<String, String> consumer =
                     new KafkaConsumer<>(properties)) {

            consumer.subscribe(List.of(TOPIC));

            // Consumer가 파티션을 할당받을 때까지 대기
            long assignmentDeadline =
                    System.nanoTime() + Duration.ofSeconds(15).toNanos();

            while (consumer.assignment().isEmpty()
                    && System.nanoTime() < assignmentDeadline) {
                consumer.poll(Duration.ofMillis(200));
            }

            assertThat(consumer.assignment())
                    .as("Kafka Consumer 파티션 할당")
                    .isNotEmpty();

            // 기존 메시지를 건너뛰고 새로 발행할 메시지만 수신
            consumer.seekToEnd(consumer.assignment());
            consumer.assignment().forEach(consumer::position);

            // when
            kafkaTemplate.send(
                    TOPIC,
                    userId.toString(),
                    payload
            ).get();

            // then
            ConsumerRecord<String, String> received = null;

            long receiveDeadline =
                    System.nanoTime() + Duration.ofSeconds(15).toNanos();

            while (received == null
                    && System.nanoTime() < receiveDeadline) {

                ConsumerRecords<String, String> records =
                        consumer.poll(Duration.ofMillis(500));

                for (ConsumerRecord<String, String> record : records) {
                    if (userId.toString().equals(record.key())) {
                        received = record;
                        break;
                    }
                }
            }

            assertThat(received)
                    .as("Kafka Consumer 수신 메시지")
                    .isNotNull();

            assertThat(received.topic())
                    .isEqualTo(TOPIC);

            assertThat(received.key())
                    .isEqualTo(userId.toString());

            RoleUpdatedEvent receivedEvent =
                    objectMapper.readValue(
                            received.value(),
                            RoleUpdatedEvent.class
                    );

            assertThat(receivedEvent.userId())
                    .isEqualTo(userId);

            assertThat(receivedEvent.oldRole())
                    .isEqualTo(Role.USER);

            assertThat(receivedEvent.newRole())
                    .isEqualTo(Role.CHANNEL_MANAGER);
        }
    }

    private void createTestTopic() throws Exception {
        try (AdminClient adminClient = AdminClient.create(
                Map.of("bootstrap.servers", bootstrapServers)
        )) {
            try {
                adminClient.createTopics(
                        List.of(new NewTopic(TOPIC, 1, (short) 1))
                ).all().get();
            } catch (ExecutionException e) {
                if (!(e.getCause() instanceof TopicExistsException)) {
                    throw e;
                }
            }
        }
    }
}
