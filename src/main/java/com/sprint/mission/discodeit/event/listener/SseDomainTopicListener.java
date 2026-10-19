package com.sprint.mission.discodeit.event.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.event.SseEvent;
import com.sprint.mission.discodeit.service.SseEmitterManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseDomainTopicListener {

  private final ObjectMapper objectMapper;
  private final SseEmitterManager sseEmitterManager;

  @KafkaListener(
      topics = "discodeit.SseDomainEvent",
      groupId = "${discodeit.kafka.sse-group-id:discodeit-sse-local}"
  )
  public void onEvent(String payload)
      throws JsonProcessingException {

    SseEvent event = objectMapper.readValue(
        payload,
        SseEvent.class
    );

    sseEmitterManager.send(event);

    log.info(
        "SSE 도메인 이벤트 전달: eventId={}, eventName={}, receiverId={}",
        event.id(),
        event.name(),
        event.receiverId()
    );
  }
}
