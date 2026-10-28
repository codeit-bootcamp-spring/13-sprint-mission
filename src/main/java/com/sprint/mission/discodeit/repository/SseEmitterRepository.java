package com.sprint.mission.discodeit.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class SseEmitterRepository {

  private final ConcurrentMap<UUID, List<SseEmitter>> data =
      new ConcurrentHashMap<>();

  public void save(UUID receiverId, SseEmitter emitter) {
    data.compute(receiverId, (id, emitters) -> {
      if (emitters == null) {
        emitters = new CopyOnWriteArrayList<>();
      }

      emitters.add(emitter);
      return emitters;
    });
  }

  public List<SseEmitter> findAllByReceiverId(UUID receiverId) {
    List<SseEmitter> emitters = data.get(receiverId);

    return emitters == null ? List.of() : List.copyOf(emitters);
  }

  public Map<UUID, List<SseEmitter>> findAll() {
    Map<UUID, List<SseEmitter>> result = new HashMap<>();

    data.forEach((receiverId, emitters) ->
        result.put(receiverId, List.copyOf(emitters))
    );

    return Map.copyOf(result);
  }

  public void delete(UUID receiverId, SseEmitter emitter) {
    data.computeIfPresent(receiverId, (id, emitters) -> {
      emitters.remove(emitter);

      return emitters.isEmpty() ? null : emitters;
    });
  }
}