package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.sse.SseMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.springframework.stereotype.Repository;

@Repository
public class SseMessageRepository {

    private final ConcurrentLinkedDeque<UUID> eventIdQueue =
            new ConcurrentLinkedDeque<>();

    private final Map<UUID, SseMessage> messages =
            new ConcurrentHashMap<>();

    public SseMessage save(
            Collection<UUID> receiverIds,
            String eventName,
            Object data
    ) {
        UUID eventId = UUID.randomUUID();

        SseMessage message = new SseMessage(
                eventId,
                receiverIds,
                eventName,
                data
        );

        messages.put(eventId, message);
        eventIdQueue.addLast(eventId);

        return message;
    }

    public List<SseMessage> findAllAfter(
            UUID lastEventId,
            UUID receiverId
    ) {
        if (lastEventId == null) {
            return List.of();
        }

        List<SseMessage> result = new ArrayList<>();
        boolean foundLastEvent = false;

        for (UUID eventId : eventIdQueue) {
            if (!foundLastEvent) {
                foundLastEvent = eventId.equals(lastEventId);
                continue;
            }

            SseMessage message = messages.get(eventId);

            if (message != null
                    && message.canReceive(receiverId)) {
                result.add(message);
            }
        }

        return List.copyOf(result);
    }
}