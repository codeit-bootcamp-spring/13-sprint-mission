package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.dto.response.NotificationDto;
import com.sprint.mission.discodeit.event.SseEvent;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SseEmitterManager {

  private final RedisSseEventStore redisSseEventStore;

  private final Map<UUID, Set<SseConnection>> connections =
      new ConcurrentHashMap<>();

  private static class SseConnection {

    private final SseEmitter emitter =
        new SseEmitter(30 * 60 * 1000L);

    private final Set<UUID> deliveredEventIds =
        new HashSet<>();
  }

  public SseEmitter subscribe(UUID userId) {
    return subscribe(userId, null);
  }

  public SseEmitter subscribe(
      UUID userId,
      UUID lastEventId
  ) {
    SseConnection connection = new SseConnection();

    Runnable cleanup = () ->
        connections.computeIfPresent(userId, (id, current) -> {
          current.remove(connection);
          return current.isEmpty() ? null : current;
        });

    connection.emitter.onCompletion(cleanup);
    connection.emitter.onTimeout(cleanup);
    connection.emitter.onError(error -> cleanup.run());

    synchronized (connection) {
      connections.compute(userId, (id, current) -> {
        if (current == null) {
          current = ConcurrentHashMap.newKeySet();
        }
        current.add(connection);
        return current;
      });

      try {
        connection.emitter.send(
            SseEmitter.event()
                .name("connected")
                .data("SSE 연결 성공")
        );

        if (lastEventId != null) {
          for (SseEvent event :
              redisSseEventStore.findAfter(userId, lastEventId)) {

            if (sendEvent(
                connection,
                event.id(),
                event.name(),
                event.data()
            )) {
              log.info(
                  "SSE 누락 이벤트 복원: eventId={}, receiverId={}",
                  event.id(),
                  userId
              );
            }
          }
        }

      } catch (Exception e) {
        cleanup.run();
        connection.emitter.completeWithError(e);

        throw new IllegalStateException(
            "SSE 연결 또는 이벤트 복원 실패", e
        );
      }
    }

    return connection.emitter;
  }

  public void send(UUID userId, NotificationDto notification) {
    Set<SseConnection> userConnections =
        connections.get(userId);

    if (userConnections == null) {
      return;
    }

    for (SseConnection connection : userConnections) {
      synchronized (connection) {
        try {
          sendEvent(
              connection,
              notification.id(),
              "notifications.created",
              notification
          );
        } catch (Exception e) {
          connection.emitter.completeWithError(e);

          log.warn(
              "SSE 실시간 전송 실패: receiverId={}, eventId={}",
              userId,
              notification.id(),
              e
          );
        }
      }
    }
  }

  private boolean sendEvent(
      SseConnection connection,
      UUID eventId,
      String eventName,
      Object data
  ) throws IOException {

    int connectionId = System.identityHashCode(connection);

    if (connection.deliveredEventIds.contains(eventId)) {
      log.warn(
          "SSE 중복 차단: connection={}, eventId={}",
          connectionId,
          eventId
      );
      return false;
    }

    connection.emitter.send(
        SseEmitter.event()
            .id(eventId.toString())
            .name(eventName)
            .data(data)
    );

    connection.deliveredEventIds.add(eventId);

    log.info(
        "SSE 실제 전송: connection={}, eventId={}",
        connectionId,
        eventId
    );

    return true;
  }
}
