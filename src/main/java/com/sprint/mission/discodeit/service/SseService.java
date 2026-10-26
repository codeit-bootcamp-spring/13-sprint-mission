package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.repository.SseEmitterRepository;
import com.sprint.mission.discodeit.repository.SseMessageRepository;
import com.sprint.mission.discodeit.sse.SseMessage;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class SseService {

    private static final long SSE_TIMEOUT =
            1000L * 60 * 60;

    private final SseEmitterRepository sseEmitterRepository;
    private final SseMessageRepository sseMessageRepository;

    public SseEmitter connect(
            UUID receiverId,
            UUID lastEventId
    ) {
        SseEmitter sseEmitter =
                new SseEmitter(SSE_TIMEOUT);

        sseEmitterRepository.save(
                receiverId,
                sseEmitter
        );

        sseEmitter.onCompletion(
                () -> sseEmitterRepository.delete(
                        receiverId,
                        sseEmitter
                )
        );

        sseEmitter.onTimeout(
                () -> sseEmitterRepository.delete(
                        receiverId,
                        sseEmitter
                )
        );

        sseEmitter.onError(
                exception ->
                        sseEmitterRepository.delete(
                                receiverId,
                                sseEmitter
                        )
        );

        if (!ping(sseEmitter)) {
            sseEmitterRepository.delete(
                    receiverId,
                    sseEmitter
            );

            return sseEmitter;
        }

        List<SseMessage> missedMessages =
                sseMessageRepository.findAllAfter(
                        lastEventId,
                        receiverId
                );

        for (SseMessage message : missedMessages) {
            if (!send(sseEmitter, message)) {
                sseEmitterRepository.delete(
                        receiverId,
                        sseEmitter
                );
                break;
            }
        }

        return sseEmitter;
    }

    public void send(
            Collection<UUID> receiverIds,
            String eventName,
            Object data
    ) {
        Set<UUID> receivers = Set.copyOf(receiverIds);

        SseMessage message =
                sseMessageRepository.save(
                        receivers,
                        eventName,
                        data
                );

        for (UUID receiverId : receivers) {
            for (SseEmitter sseEmitter :
                    sseEmitterRepository
                            .findAllByReceiverId(receiverId)) {
                if (!send(sseEmitter, message)) {
                    sseEmitterRepository.delete(
                            receiverId,
                            sseEmitter
                    );
                }
            }
        }
    }

    public void broadcast(
            String eventName,
            Object data
    ) {
        SseMessage message =
                sseMessageRepository.save(
                        List.of(),
                        eventName,
                        data
                );

        Map<UUID, List<SseEmitter>> emitters =
                sseEmitterRepository.findAll();

        emitters.forEach(
                (receiverId, receiverEmitters) -> {
                    for (SseEmitter sseEmitter :
                            receiverEmitters) {
                        if (!send(sseEmitter, message)) {
                            sseEmitterRepository.delete(
                                    receiverId,
                                    sseEmitter
                            );
                        }
                    }
                }
        );
    }

    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        Map<UUID, List<SseEmitter>> emitters =
                sseEmitterRepository.findAll();

        emitters.forEach(
                (receiverId, receiverEmitters) -> {
                    for (SseEmitter sseEmitter :
                            receiverEmitters) {
                        if (!ping(sseEmitter)) {
                            sseEmitterRepository.delete(
                                    receiverId,
                                    sseEmitter
                            );
                        }
                    }
                }
        );
    }

    private boolean send(
            SseEmitter sseEmitter,
            SseMessage message
    ) {
        try {
            sseEmitter.send(
                    SseEmitter.event()
                            .id(message.id().toString())
                            .name(message.eventName())
                            .data(message.data())
            );

            return true;
        } catch (
                IOException
                | IllegalStateException exception
        ) {
            sseEmitter.complete();

            return false;
        }
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(
                    SseEmitter.event()
                            .name("ping")
                            .data("ping")
            );

            return true;
        } catch (
                IOException
                | IllegalStateException exception
        ) {
            sseEmitter.complete();

            return false;
        }
    }
}