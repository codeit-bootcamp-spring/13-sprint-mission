package com.sprint.mission.discodeit.sse;

import java.util.*;
import java.util.concurrent.*;
import java.time.*;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {
  private static final int MAX_MESSAGES = 10000;
  private static final Duration RETENTION = Duration.ofHours(1);
  private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
  private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

  public synchronized void save(SseMessage message) {
    if (messages.putIfAbsent(message.id(), message) == null) {
      eventIdQueue.addLast(message.id());
    }
    prune();
  }

  public synchronized List<SseMessage> findAfter(UUID lastId, UUID receiverId) {
    prune();
    if (!messages.containsKey(lastId)) {
      return List.of(); // 보관 범위를 벗어나면 클라이언트의 REST 재조회가 필요하다.
    }
    List<SseMessage> result = new ArrayList<>();
    boolean after = false;
    for (UUID id : eventIdQueue) {
      SseMessage message = messages.get(id);
      if (after && message != null && message.visibleTo(receiverId)) {
        result.add(message);
      }
      if (id.equals(lastId)) {
        after = true;
      }
    }
    return List.copyOf(result);
  }

  private void prune() {
    Instant cutoff = Instant.now().minus(RETENTION);
    while (!eventIdQueue.isEmpty()) {
      UUID id = eventIdQueue.peekFirst();
      SseMessage first = messages.get(id);
      if (messages.size() <= MAX_MESSAGES && first != null && !first.createdAt().isBefore(cutoff)) {
        break;
      }
      eventIdQueue.pollFirst();
      messages.remove(id);
    }
  }
}
