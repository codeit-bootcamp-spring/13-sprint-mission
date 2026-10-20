package com.sprint.mission.discodeit.sse;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Repository
// 클라이언트에서 ListEventId 를 전송해서 이벤트 유실 복원이 가능해야 함.
public class SseMessageRepository {
    private final ConcurrentLinkedDeque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();

    public SseMessage save(SseMessage message) {
        eventIdQueue.add(message.getId());
        messages.put(message.getId(), message);
        return message;
    }

    public boolean isLast(UUID messageId){
        Optional<UUID> lastId = Optional.ofNullable(eventIdQueue.peekLast());
        return lastId.map(uuid -> uuid.equals(messageId)).orElse(false);
    }

    public List<SseMessage> findAllById(UUID id, UUID lastIndex) {
        return eventIdQueue.stream()
                .skip(getIndex(lastIndex) + 1)
                .map(messages::get)
                .filter(m -> (m.getId().equals(id) || m.getId().equals(SseMessage.BROADCAST_ID))
                ).toList();
    }

    public List<SseMessage> findAll(){
        return eventIdQueue.stream().map(messages::get).toList();
    }


    private Integer getIndex(UUID value){
        int index = 0;
        if (value == null) return index;
        for (UUID key : eventIdQueue) {
            if (key.equals(value)) return index;
            index++;
        }
        return 0;
    }

}
