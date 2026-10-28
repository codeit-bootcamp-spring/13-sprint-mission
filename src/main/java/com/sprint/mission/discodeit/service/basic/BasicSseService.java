package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.dto.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.service.SseService;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicSseService implements SseService {

  private static final long TIMEOUT = 60L * 60 * 1000;

  private final SseEmitterRepository sseEmitterRepository;
  private final SseMessageRepository sseMessageRepository;

  @Override
  public synchronized SseEmitter connect(UUID receiverId, UUID lastEventId) {
    SseEmitter emitter = new SseEmitter(TIMEOUT);

    emitter.onCompletion(() ->
        sseEmitterRepository.delete(receiverId, emitter)
    );

    emitter.onTimeout(() -> {
      sseEmitterRepository.delete(receiverId, emitter);
      emitter.complete();
    });

    emitter.onError(error ->
        sseEmitterRepository.delete(receiverId, emitter)
    );

    sseEmitterRepository.save(receiverId, emitter);

    if (!ping(emitter)) {
      sseEmitterRepository.delete(receiverId, emitter);
      emitter.complete();
      return emitter;
    }

    for (SseMessage message :
        sseMessageRepository.findAllAfter(receiverId, lastEventId)) {
      if (!sendToEmitter(receiverId, emitter, message)) {
        break;
      }
    }

    return emitter;
  }

  @Override
  public synchronized void send(
      Collection<UUID> receiverIds,
      String eventName,
      Object data
  ) {
    if (receiverIds.isEmpty()) {
      return;
    }

    SseMessage message = new SseMessage(
        UUID.randomUUID(),
        Set.copyOf(receiverIds),
        eventName,
        data,
        false
    );

    sseMessageRepository.save(message);

    for (UUID receiverId : message.receiverIds()) {
      for (SseEmitter emitter :
          sseEmitterRepository.findAllByReceiverId(receiverId)) {
        sendToEmitter(receiverId, emitter, message);
      }
    }
  }

  @Override
  public synchronized void broadcast(String eventName, Object data) {
    SseMessage message = new SseMessage(
        UUID.randomUUID(),
        Set.of(),
        eventName,
        data,
        true
    );

    sseMessageRepository.save(message);

    sseEmitterRepository.findAll().forEach((receiverId, emitters) -> {
      for (SseEmitter emitter : emitters) {
        sendToEmitter(receiverId, emitter, message);
      }
    });
  }

  @Override
  @Scheduled(fixedDelay = 1000 * 60 * 30)
  public void cleanUp() {
    sseEmitterRepository.findAll().forEach((receiverId, emitters) -> {
      for (SseEmitter emitter : emitters) {
        if (!ping(emitter)) {
          sseEmitterRepository.delete(receiverId, emitter);
        }
      }
    });
  }

  private boolean sendToEmitter(
      UUID receiverId,
      SseEmitter emitter,
      SseMessage message
  ) {
    try {
      emitter.send(
          SseEmitter.event()
              .id(message.id().toString())
              .name(message.eventName())
              .data(message.data())
      );

      return true;
    } catch (IOException | IllegalStateException e) {
      sseEmitterRepository.delete(receiverId, emitter);

      log.debug(
          "SSE 전송 실패 - receiverId: {}, eventId: {}",
          receiverId,
          message.id(),
          e
      );

      return false;
    }
  }

  private boolean ping(SseEmitter emitter) {
    try {
      emitter.send(
          SseEmitter.event()
              .name("ping")
              .data("")
      );

      return true;
    } catch (IOException | IllegalStateException e) {
      return false;
    }
  }
}