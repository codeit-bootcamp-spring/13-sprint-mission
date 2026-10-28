package com.sprint.mission.discodeit.realtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.sse.*;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class RealtimeEventListener {
  private final KafkaTemplate<String, String> kafka;
  private final ObjectMapper mapper;
  private final SimpMessagingTemplate websocket;
  private final SseService sse;

  @TransactionalEventListener(fallbackExecution = true)
  public void publish(RealtimeEvent event) throws JsonProcessingException {
    // 동일한 키의 이벤트는 동일 파티션으로 보내 순서를 보존한다.
    String key = event.destination() != null ? event.destination()
        : event.data().hasNonNull("id") ? event.data().get("id").asText() : event.eventName();
    kafka.send("discodeit.RealtimeEvent", key, mapper.writeValueAsString(event))
        .whenComplete((result, error) -> {
          if (error != null) {
            org.slf4j.LoggerFactory.getLogger(getClass()).error("실시간 이벤트 발행 실패", error);
          }
        });
  }

  // 알림 생성용 공통 그룹과 달리 서버마다 별도 그룹으로 모든 이벤트를 수신한다.
  @KafkaListener(topics = "discodeit.RealtimeEvent",
      groupId = "discodeit-realtime-${discodeit.instance-id}",
      properties = "auto.offset.reset=latest")
  public void consume(String json) throws JsonProcessingException {
    RealtimeEvent event = mapper.readValue(json, RealtimeEvent.class);
    if (event.destination() != null) {
      websocket.convertAndSend(event.destination(), event.data());
    } else {
      sse.accept(new SseMessage(event.id(), event.createdAt(), event.receiverIds(),
          event.eventName(), event.data()));
    }
  }
}
