package com.sprint.mission.discodeit.sse;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 저장 이후 발행된 SseMessage를 이 서버의 연결에 전달하는 공통 진입점이다.
 * 인메모리 이벤트와 Redis 구독 이벤트가 모두 이 경로를 사용하며 여기서는 재저장하지 않는다.
 */
@Component
@RequiredArgsConstructor
public class SseMessageDispatcher {

    private final SseMessageReader messageReader;
    private final SseService sseService;

    @EventListener
    public void handle(SseMessage message){
        SseDelivery delivery = messageReader.toDelivery(message);

        if (message.broadcast()){
            sseService.broadcast(delivery);
            return;
        }

        sseService.send(message.receiverIds(), delivery);
    }
}
