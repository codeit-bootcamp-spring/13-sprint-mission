package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository {

    private final ConcurrentLinkedDeque<UUID> eventIdQueue =
            new ConcurrentLinkedDeque<>();

    private final Map<UUID, SseMessage> messages =
            new ConcurrentHashMap<>();

    public void save(SseMessage message) {
        messages.put(message.id(), message);
        eventIdQueue.addLast(message.id());
    }

    public List<SseMessage> findAllByUserIdAndIdAfter(
            UUID userId,
            UUID lastEventId
    ) {
        List<SseMessage> result = new ArrayList<>();

        if (lastEventId == null) {
            return result;
        }

        boolean foundLastEvent = false;

        for (UUID eventId : eventIdQueue) {

            if (!foundLastEvent) {
                if (eventId.equals(lastEventId)) {
                    foundLastEvent = true;
                }
                continue;
            }

            SseMessage message = messages.get(eventId);

            if (message != null
                    && message.userId().equals(userId)) {
                result.add(message);
            }
        }

        return result;
    }

    public void deleteBefore(Instant cutoff) {
        for (UUID eventId : eventIdQueue) {
            SseMessage message = messages.get(eventId);

            if (message != null
                    && message.createdAt().isBefore(cutoff)) {
                if (messages.remove(eventId, message)) {
                    eventIdQueue.remove(eventId);
                }
            }
        }
    }
}
