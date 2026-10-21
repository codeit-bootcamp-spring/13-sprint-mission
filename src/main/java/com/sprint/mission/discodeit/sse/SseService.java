
package com.sprint.mission.discodeit.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final long SSE_TIMEOUT_MILLIS =
            Duration.ofMinutes(30).toMillis();

    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;

    public SseEmitter connect(UUID userId, Long lastEventId) {

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);

        emitterRepository.save(userId, emitter);

        emitter.onCompletion(() ->
                emitterRepository.delete(userId, emitter)
        );

        emitter.onTimeout(() ->
                emitterRepository.delete(userId, emitter)
        );

        emitter.onError(error -> {
            log.warn("SSE 연결 오류. userId={}", userId, error);
            emitterRepository.delete(userId, emitter);
        });

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("SSE 연결 성공")
            );

            if (lastEventId != null) {

                for (SseMessage message :
                        messageRepository.findAllByUserIdAndIdGreaterThan(
                                userId, lastEventId)) {

                    emitter.send(
                            SseEmitter.event()
                                    .id(String.valueOf(message.id()))
                                    .name(message.eventName())
                                    .data(message.data())
                    );
                }
            }

            log.info("SSE 연결 성공. userId={}, lastEventId={}",
                    userId, lastEventId);

        } catch (IOException | IllegalStateException e) {
            emitterRepository.delete(userId, emitter);
            emitter.completeWithError(e);

            log.warn("SSE 연결 또는 이벤트 복구 실패. userId={}",
                    userId, e);
        }

        return emitter;
    }

    private final AtomicLong eventSequence = new AtomicLong();

    public void send(UUID receiverId, String eventName, Object data) {

        long eventId = eventSequence.incrementAndGet();

        SseMessage message = new SseMessage(
                eventId,
                receiverId,
                eventName,
                data,
                Instant.now()
        );

        messageRepository.save(message);

        for (SseEmitter emitter : emitterRepository.findAllByUserId(receiverId)) {
            try {
                emitter.send(
                        SseEmitter.event()
                                .id(String.valueOf(eventId))
                                .name(eventName)
                                .data(data)
                );

            } catch (IOException | IllegalStateException e) {
                emitterRepository.delete(receiverId, emitter);

                log.warn(
                        "SSE 이벤트 전송 실패. receiverId={}, eventId={}",
                        receiverId,
                        eventId,
                        e
                );
            }
        }
    }

    public void broadcast(String eventName, Object data) {

        Set<UUID> receiverIds = emitterRepository.findAllUserIds();

        for (UUID receiverId : receiverIds) {
            send(receiverId, eventName, data);
        }

        log.info(
                "SSE 전체 이벤트 전송. eventName={}, receiverCount={}",
                eventName,
                receiverIds.size()
        );
    }

    private boolean ping(SseEmitter emitter) {
        try {
            emitter.send(
                    SseEmitter.event()
                            .name("ping")
                            .data("keep-alive")
            );

            return true;

        } catch (IOException | IllegalStateException e) {
            log.debug("SSE ping 전송 실패", e);
            return false;
        }
    }

    @Scheduled(fixedDelayString = "PT30M")
    public void cleanUp() {

        emitterRepository.forEach((userId, emitter) -> {
            if (!ping(emitter)) {
                emitterRepository.delete(userId, emitter);
            }
        });

        log.info("SSE 연결 정리 작업 완료");
    }




}
