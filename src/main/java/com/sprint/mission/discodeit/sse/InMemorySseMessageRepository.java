package com.sprint.mission.discodeit.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 단일 서버에서 사용할 SSE 이력과 저장 순번을 메모리에 보관한다.
 * 이력은 서버 재시작 시 사라지고 다른 인스턴스와 공유되지 않는다.
 * 현재 별도 이력 만료 정책은 없으며, 발행은 로컬 Spring 이벤트로 연결한다.
 */
@Repository
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "discodeit.security.jwt",
        name = "registry-type",
        havingValue = "in-memory"
        ,matchIfMissing = true
)
public class InMemorySseMessageRepository implements SseMessageRepository{

    private final ApplicationEventPublisher eventPublisher;
    private final Deque<UUID> eventIdQueue = new ConcurrentLinkedDeque<>();
    private final Map<UUID, SseMessage> messages = new ConcurrentHashMap<>();
    private final Map<UUID, Long> sequences = new ConcurrentHashMap<>();
    private long sequence = 0L;

    /**
     * 중복 검사부터 순번·본문·큐 갱신과 발행까지 하나의 JVM 잠금 안에서 실행한다.
     * AtomicLong만 사용하면 여러 자료구조의 갱신 순서까지 보호할 수 없다.
     * 동일 eventId는 false를 반환하고 다시 발행하지 않는다.
     */
    @Override
    public synchronized boolean saveAndPublish(SseMessage message){
        UUID eventId = message.eventId();
        if (messages.containsKey(eventId)){
            return false;
        }
        long nextSequence = ++sequence;

        messages.put(eventId, message);
        sequences.put(eventId, nextSequence);
        eventIdQueue.add(eventId);

        eventPublisher.publishEvent(message);

        return true;
    }

    @Override
    public List<SseMessage> findAllAfter(UUID receiverId, UUID lastEventId){
        if(lastEventId == null){
            return List.of();
        }

        boolean foundLastEvent = false;

        List< SseMessage> results = new LinkedList<>();

        for(UUID eventId : eventIdQueue){
            if (!foundLastEvent){
                if (eventId.equals(lastEventId)){
                    foundLastEvent = true;
                }
                continue;
            }

            SseMessage sseMessage = messages.get(eventId);
            if (sseMessage != null && (sseMessage.broadcast() || sseMessage.receiverIds().contains(receiverId))){
                results.add(sseMessage);
            }
        }

        return Collections.unmodifiableList(results);
    }

    @Override
    public OptionalLong findSequence(UUID eventId){
        if (eventId == null){
            return OptionalLong.empty();
        }
        Long storedSequence = sequences.get(eventId);
        if(storedSequence == null){
            return OptionalLong.empty();
        }

        return OptionalLong.of(storedSequence);
    }
}
