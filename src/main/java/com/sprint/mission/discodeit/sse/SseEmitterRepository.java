package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@Repository
public class SseEmitterRepository {

    private final ConcurrentMap<UUID, CopyOnWriteArrayList<SseEmitter>>
            emitters = new ConcurrentHashMap<>();


    public void save(UUID userId, SseEmitter emitter) {

        emitters.computeIfAbsent(
                userId,
                id -> new CopyOnWriteArrayList<>()
        ).add(emitter);
    }

    public List<SseEmitter> findAllByUserId(UUID userId) {

        return List.copyOf(
                emitters.getOrDefault(
                        userId,
                        new CopyOnWriteArrayList<>()
                )
        );
    }

    public List<SseEmitter> findAll() {

        return emitters.values().stream()
                .flatMap(List::stream)
                .toList();
    }

    public void delete(UUID userId, SseEmitter emitter) {

        emitters.computeIfPresent(userId, (id, connections) -> {

            connections.remove(emitter);

            return connections.isEmpty() ? null : connections;
        });
    }


    public Set<UUID> findAllUserIds() {
        return Set.copyOf(emitters.keySet());
    }

    public void forEach(BiConsumer<UUID, SseEmitter> action) {

        emitters.forEach((userId, connections) -> {
            for (SseEmitter emitter : connections) {
                action.accept(userId, emitter);
            }
        });
    }
}
