package com.sprint.mission.discodeit.sse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    private final String NAME = "SseService";

    private final SseEmitterRepository emitterRepository;
    private final SseMessageRepository messageRepository;

    /**
     * SSE 를 통한 이벤트 발송을 위한 매서드
     * @param receiverId - 로그인 된 유저 id?
     * @param lastEventId - 이벤트 유실 방지를 위한 이벤트
     * @return 클라이언트에 전달 할 Emitter 주소?
     */
    public SseEmitter connect(UUID receiverId, UUID lastEventId) {

        log.debug("[{}] - SSE connection start - receiver : {}, lastEventId : {}",
                NAME,
                receiverId,
                lastEventId);

        SseEmitter emitter = new SseEmitter();  // 타임아웃 시간을 어떻게 정할 지 모르니 일단 공백.
        emitterRepository.register(receiverId, emitter);

        emitter.onTimeout(emitter::complete);
        emitter.onCompletion(() -> emitterRepository.delete(receiverId,emitter));
        emitter.onError(e -> {
            log.error("[{}] - SSE Emitter error!", NAME, e);
            emitterRepository.delete(receiverId,emitter);
        });

        sendToEmitter(emitter, messageRepository.findAllById(receiverId,lastEventId));

        return emitter;
    }


    public void send(Collection<UUID> receiverIds, String eventName, Object data) {

        log.debug(
                "[{}] - SSE Message send. - eventName : {}, data : {}",
                NAME,
                eventName,
                data
        );

        // 채팅방에서 본인이 쓴 메세지의 정보도 알림으로 나옴.
        // message 가
        // 어떤 에미터가 쓴 것인지? -> receiverId

        receiverIds.forEach(receiverId -> {
            SseMessage message = new SseMessage(receiverId, eventName,data);
            messageRepository.save(message);
            emitterRepository.findByReceiverList(List.of(receiverId))
                    .forEach(emitter -> {
                        sendToEmitter(emitter, List.of(message));
                    });
        });


    }

    public void broadcast(String eventName, Object data) {
        log.debug(
                "[{}] - SSE Message broadcast. - eventName : {}, data : {}",
                NAME,
                eventName,
                data
        );

        SseMessage message = new SseMessage(SseMessage.BROADCAST_ID, eventName, data);
        messageRepository.save(message);
        emitterRepository.findAll()
                .forEach(emitter -> {
                    sendToEmitter(emitter, List.of(message));
                });
    }


    /*
    주기적으로 ping 을 보낸 후, 죽은 emitter 삭제
     */
    @Scheduled(fixedDelay = 1000 * 60 * 30)
    public void cleanUp() {
        emitterRepository.findAll().forEach(emitter -> {
            if (!ping(emitter)) // ping 이 false => 연결 끊김
                emitterRepository.delete(emitter);
        });
    }

    private boolean ping(SseEmitter sseEmitter) {
        try {
            sseEmitter.send(SseEmitter.event().comment("ping"));
            return true;
        } catch (IOException | IllegalStateException e) {
            log.warn("[{}] - {} emitter ping failed",NAME, sseEmitter);
            return false;
        }
    }

    private void sendToEmitter(SseEmitter emitter, List<SseMessage> list) {
        list.forEach( message -> {
            try{
                log.debug("[{}] - SSE Emitter message send", NAME);
                emitter.send(
                        SseEmitter.event()
                                .id(message.getId().toString())
                                .name(message.getName())
                                .data(message.getContent())
                );
            } catch (IOException e){
                log.warn("[{}] - SSE message send failure : {}", NAME, emitter);
                throw new RuntimeException(e);
            }
        });
    }
}

