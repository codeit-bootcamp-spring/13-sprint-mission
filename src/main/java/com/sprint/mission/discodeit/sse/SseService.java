 package com.sprint.mission.discodeit.sse;

 import lombok.RequiredArgsConstructor;
 import lombok.extern.slf4j.Slf4j;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

 import java.io.IOException;
 import java.time.Duration;
 import java.time.Instant;
 import java.util.Collection;
 import java.util.Set;
 import java.util.UUID;

 @Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private static final long SSE_TIMEOUT_MILLIS =
            Duration.ofMinutes(30).toMillis();

    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;

    public SseEmitter connect(UUID receiverId, UUID lastEventId) {

        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MILLIS);

        emitterRepository.save(receiverId, emitter);

        emitter.onCompletion(() ->
                emitterRepository.delete(receiverId, emitter)
        );

        emitter.onTimeout(() ->
                emitterRepository.delete(receiverId, emitter)
        );

        emitter.onError(error -> {
            log.warn("SSE 연결 오류. receiverId={}", receiverId, error);
            emitterRepository.delete(receiverId, emitter);
        });

        try {
            emitter.send(
                    SseEmitter.event()
                            .name("connected")
                            .data("SSE 연결 성공")
            );

            if (lastEventId != null) {
                for (SseMessage message :
                        messageRepository.findAllByUserIdAndIdAfter(
                                receiverId, lastEventId
                        )) {

                    emitter.send(
                            SseEmitter.event()
                                    .id(message.id().toString())
                                    .name(message.eventName())
                                    .data(message.data())
                    );
                }
            }

            log.info("SSE 연결 성공. receiverId={}, lastEventId={}",
                    receiverId, lastEventId);

        } catch (IOException | IllegalStateException e) {
            emitterRepository.delete(receiverId, emitter);
            emitter.completeWithError(e);

            log.warn("SSE 연결 또는 이벤트 복구 실패. receiverId={}",
                    receiverId, e);
        }

        return emitter;
    }

    public void send(Collection<UUID> receiverIds, String eventName, Object data)  {

        for (UUID receiverId : receiverIds) {

            UUID eventId = UUID.randomUUID();

            SseMessage message = new SseMessage(
                    eventId,
                    receiverId,
                    eventName,
                    data,
                    Instant.now()
            );

            messageRepository.save(message);

            for (SseEmitter emitter :
                    emitterRepository.findAllByUserId(receiverId)) {

                try {
                    emitter.send(
                            SseEmitter.event()
                                    .id(eventId.toString())
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
    }

    public void broadcast(String eventName, Object data) {

        Set<UUID> receiverIds = emitterRepository.findAllUserIds();

        send(receiverIds, eventName, data);

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

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {

        emitterRepository.forEach((userId, emitter) -> {
            if (!ping(emitter)) {
                emitterRepository.delete(userId, emitter);
            }
        });

        log.info("SSE 연결 정리 작업 완료");
    }




}
