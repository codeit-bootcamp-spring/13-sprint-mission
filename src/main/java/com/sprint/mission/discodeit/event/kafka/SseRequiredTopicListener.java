package com.sprint.mission.discodeit.event.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SseRequiredTopicListener {

    private final ObjectMapper objectMapper;
    private final SseService sseService;

    @KafkaListener(
            topics = "discodeit.sse.events",
            groupId = "discodeit-sse-${HOSTNAME:local}",
            properties = "auto.offset.reset=latest"
    )
    public void onSseEvent(String kafkaEvent) {
        try {
            SseKafkaEvent event = objectMapper.readValue(kafkaEvent, SseKafkaEvent.class);

            if (event.receiverIds() == null) {
                sseService.broadcast(event.eventName(), event.data());
            } else {
                sseService.send(
                        event.receiverIds(),
                        event.eventName(),
                        event.data()
                );
            }

            log.info("SSE Kafka 이벤트 수신 완료. eventName={}", event.eventName());

        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "SSE Kafka 이벤트 역직렬화에 실패했습니다.", e
            );
        }
    }
}
