package com.sprint.mission.discodeit.sse;

import java.util.*;
import java.util.concurrent.*;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {
  private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

  public void add(UUID id, SseEmitter emitter) {
    data.compute(id, (key, current) -> {
      List<SseEmitter> emitters = current == null ? new CopyOnWriteArrayList<>() : current;
      emitters.add(emitter);
      return emitters;
    });
  }

  public void remove(UUID id, SseEmitter emitter) {
    data.computeIfPresent(id, (key, emitters) -> {
      emitters.remove(emitter);
      return emitters.isEmpty() ? null : emitters;
    });
  }

  public List<SseEmitter> find(UUID id) {
    return List.copyOf(data.getOrDefault(id, List.of()));
  }

  public Set<UUID> receiverIds() {
    return Set.copyOf(data.keySet());
  }
}
