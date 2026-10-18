package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.event.SseMessage;
import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SseService {

    private static final long SSE_TIMEOUT = 1000L * 60 * 60;

    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;

    // SSE 연결 및 유실 이벤트 복원
    public SseEmitter connect(UUID receiverId, UUID lastEventId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        emitter.onCompletion(() ->
                emitterRepository.delete(receiverId, emitter));

        emitter.onTimeout(() ->
                emitterRepository.delete(receiverId, emitter));

        emitter.onError(error ->
                emitterRepository.delete(receiverId, emitter));

        emitterRepository.save(receiverId, emitter);

        // 최초 연결 확인
        if (!ping(emitter)) {
            emitterRepository.delete(receiverId, emitter);
            return emitter;
        }

        // 마지막 수신 이벤트 이후의 메시지 복원
        if (lastEventId != null) {
            List<SseMessage> missedMessages = messageRepository.findAfter(receiverId, lastEventId);

            for (SseMessage message : missedMessages) {
                if (!sendEvent(emitter, message)) {
                    emitterRepository.delete(receiverId, emitter);
                    break;
                }
            }
        }

        return emitter;
    }

    // 특정 사용자들에게 이벤트 전송
    public void send(
            Collection<UUID> receiverIds,
            String eventName,
            Object data
    ) {
        if (receiverIds.isEmpty()) {
            return;
        }

        SseMessage message = messageRepository.save(receiverIds, eventName, data);

        for (UUID receiverId : receiverIds) {
            for (SseEmitter emitter : emitterRepository.findByReceiverId(receiverId)) {
                if (!sendEvent(emitter, message)) {
                    emitterRepository.delete(receiverId, emitter);
                }
            }
        }
    }

    // 전체 연결 사용자에게 이벤트 전송
    public void broadcast(String eventName, Object data) {
        SseMessage message = messageRepository.save(List.of(), eventName, data);

        for (UUID receiverId : emitterRepository.findAllReceiverIds()) {
            for (SseEmitter emitter : emitterRepository.findByReceiverId(receiverId)) {
                if (!sendEvent(emitter, message)) {
                    emitterRepository.delete(receiverId, emitter);
                }
            }
        }
    }

    // 30분마다 연결 상태 확인
    @Scheduled(fixedDelay = 1000L * 60 * 30)
    public void cleanUp() {
        for (UUID receiverId : emitterRepository.findAllReceiverIds()) {
            for (SseEmitter emitter : emitterRepository.findByReceiverId(receiverId)) {
                if (!ping(emitter)) {
                    emitterRepository.delete(receiverId, emitter);
                }
            }
        }
    }

    // 연결 확인용 더미 이벤트
    private boolean ping(SseEmitter emitter) {
        try {
            emitter.send(SseEmitter.event().name("ping").data(""));
            return true;
        } catch (IOException | IllegalStateException e) {
            return false;
        }
    }

    // 실제 SSE 이벤트 전송
    private boolean sendEvent(SseEmitter emitter, SseMessage message) {
        try {
            emitter.send(SseEmitter.event()
                    .id(message.id().toString())
                    .name(message.eventName())
                    .data(message.data()));
            return true;
        } catch (IOException | IllegalStateException e) {
            return false;
        }
    }
}
