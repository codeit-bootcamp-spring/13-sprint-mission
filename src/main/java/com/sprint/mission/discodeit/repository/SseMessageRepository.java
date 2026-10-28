package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.dto.SseMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

  private static final int MAX_SIZE = 1000;

  private final ConcurrentLinkedDeque<UUID> eventQueue =
      new ConcurrentLinkedDeque<>();

  private final Map<UUID, SseMessage> messages =
      new ConcurrentHashMap<>();

  public synchronized void save(SseMessage message) {
    messages.put(message.id(), message);
    eventQueue.addLast(message.id());

    while (eventQueue.size() > MAX_SIZE) {
      UUID oldestId = eventQueue.pollFirst();

      if (oldestId != null) {
        messages.remove(oldestId);
      }
    }
  }

  public synchronized List<SseMessage> findAllAfter(
      UUID receiverId,
      UUID lastEventId
  ) {
    if (lastEventId == null) {
      return List.of();
    }

    List<SseMessage> result = new ArrayList<>();
    boolean afterLastEvent = !messages.containsKey(lastEventId);

    for (UUID eventId : eventQueue) {
      if (!afterLastEvent) {
        if (eventId.equals(lastEventId)) {
          afterLastEvent = true;
        }

        continue;
      }

      SseMessage message = messages.get(eventId);

      if (message != null && message.isVisibleTo(receiverId)) {
        result.add(message);
      }
    }

    return List.copyOf(result);
  }
}