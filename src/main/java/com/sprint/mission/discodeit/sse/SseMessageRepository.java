package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
public class SseMessageRepository {

    private final ConcurrentLinkedDeque<SseMessage> messages =
            new ConcurrentLinkedDeque<>();

    public void save(SseMessage message) {
        messages.addLast(message);
    }

    public List<SseMessage> findAllByUserIdAndIdGreaterThan(
            UUID userId,
            long lastEventId
    ) {
        return messages.stream()
                .filter(message -> message.userId().equals(userId))
                .filter(message -> message.id() > lastEventId)
                .sorted(java.util.Comparator.comparingLong(SseMessage::id))
                .toList();
    }

    public void deleteBefore(java.time.Instant cutoff) {
        messages.removeIf(
                message -> message.createdAt().isBefore(cutoff)
        );
    }
}