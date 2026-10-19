package com.sprint.mission.discodeit.repository;

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

    public SseEmitter save(
            UUID receiverId,
            SseEmitter sseEmitter
    ) {
        data.computeIfAbsent(
                receiverId,
                ignored -> new CopyOnWriteArrayList<>()
        ).add(sseEmitter);

        return sseEmitter;
    }

    public List<SseEmitter> findAllByReceiverId(
            UUID receiverId
    ) {
        return List.copyOf(
                data.getOrDefault(
                        receiverId,
                        List.of()
                )
        );
    }

    public Map<UUID, List<SseEmitter>> findAll() {
        Map<UUID, List<SseEmitter>> snapshot =
                new ConcurrentHashMap<>();

        data.forEach(
                (receiverId, emitters) ->
                        snapshot.put(
                                receiverId,
                                List.copyOf(emitters)
                        )
        );

        return Map.copyOf(snapshot);
    }

    public void delete(
            UUID receiverId,
            SseEmitter sseEmitter
    ) {
        data.computeIfPresent(
                receiverId,
                (ignored, emitters) -> {
                    emitters.remove(sseEmitter);

                    return emitters.isEmpty()
                            ? null
                            : emitters;
                }
        );
    }
}