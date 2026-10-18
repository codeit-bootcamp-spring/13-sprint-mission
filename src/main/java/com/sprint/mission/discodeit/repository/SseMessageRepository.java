package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.event.SseMessage;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository {

    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();

    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public synchronized SseMessage save(
            Collection<UUID> receiverIds,
            String eventName,
            Object data
    ) {
        UUID eventId = UUID.randomUUID();

        SseMessage message = new SseMessage(
                eventId,
                Set.copyOf(receiverIds),
                eventName,
                data
        );

        messages.put(eventId, message);
        eventIdQueue.addLast(eventId);

        return message;
    }

    public synchronized List<SseMessage> findAfter(UUID receiverId, UUID lastEventId) {
        List<UUID> eventIds = new ArrayList<>(eventIdQueue);

        int lastIndex = eventIds.indexOf(lastEventId);

        if (lastIndex == -1) {
            return List.of();
        }

        return eventIds.subList(lastIndex + 1, eventIds.size())
                .stream()
                .map(messages::get)
                .filter(Objects::nonNull)
                .filter(message ->
                        message.receiverIds().isEmpty() || message.receiverIds().contains(receiverId)
                )
                .toList();
    }
}
