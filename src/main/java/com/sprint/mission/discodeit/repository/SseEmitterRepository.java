package com.sprint.mission.discodeit.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class SseEmitterRepository {

    private final ConcurrentMap<UUID, List<SseEmitter>> data = new ConcurrentHashMap<>();

    public void save(UUID receiverId, SseEmitter emitter) {
        data.computeIfAbsent(receiverId, key -> new CopyOnWriteArrayList<>())
                .add(emitter);
    }

    public List<SseEmitter> findByReceiverId(UUID receiverId) {
        List<SseEmitter> emitters = data.get(receiverId);

        return emitters == null
                ? List.of()
                : List.copyOf(emitters);
    }

    public Set<UUID> findAllReceiverIds() {
        return Set.copyOf(data.keySet());
    }

    public void delete(UUID receiverId, SseEmitter emitter) {
        data.computeIfPresent(receiverId, (key, emitters) -> {
            emitters.remove(emitter);
            return emitters.isEmpty() ? null : emitters;
        });
    }
}
