package com.sprint.mission.discodeit.sse;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {
  private final SseEmitterRepository emitters;
  private final SseMessageRepository messages;

  // 연결 등록/재전송과 실시간 전송을 직렬화해 중간 이벤트 유실을 방지한다.
  public synchronized SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(60L * 60 * 1000);
    emitter.onCompletion(() -> emitters.remove(receiverId, emitter));
    emitter.onTimeout(() -> { emitters.remove(receiverId, emitter); emitter.complete(); });
    emitter.onError(error -> emitters.remove(receiverId, emitter));
    emitters.add(receiverId, emitter);
    if (!ping(emitter)) {
      emitters.remove(receiverId, emitter);
      return emitter;
    }
    if (lastEventId != null) {
      for (SseMessage message : messages.findAfter(lastEventId, receiverId)) {
        deliver(receiverId, emitter, message);
      }
    }
    return emitter;
  }

  public void send(Collection<UUID> receiverIds, String eventName, Object data) {
    accept(new SseMessage(UUID.randomUUID(), Instant.now(), Set.copyOf(receiverIds), eventName, data));
  }

  public void broadcast(String eventName, Object data) {
    accept(new SseMessage(UUID.randomUUID(), Instant.now(), null, eventName, data));
  }

  public synchronized void accept(SseMessage message) {
    messages.save(message); // 접속하지 않은 사용자의 이벤트도 보관한다.
    for (UUID id : emitters.receiverIds()) {
      if (message.visibleTo(id)) {
        for (SseEmitter emitter : emitters.find(id)) {
          deliver(id, emitter, message);
        }
      }
    }
  }

  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    for (UUID id : emitters.receiverIds()) {
      for (SseEmitter emitter : emitters.find(id)) {
        if (!ping(emitter)) {
          emitters.remove(id, emitter);
        }
      }
    }
  }

  private boolean ping(SseEmitter emitter) {
    try {
      emitter.send(SseEmitter.event().comment("ping")); // 재연결 커서를 변경하지 않는다.
      return true;
    } catch (IOException | IllegalStateException exception) {
      emitter.completeWithError(exception);
      return false;
    }
  }

  private void deliver(UUID receiverId, SseEmitter emitter, SseMessage message) {
    try {
      emitter.send(SseEmitter.event().id(message.id().toString())
          .name(message.eventName()).data(message.data()));
    } catch (IOException | IllegalStateException exception) {
      emitters.remove(receiverId, emitter);
      emitter.completeWithError(exception);
    }
  }
}
