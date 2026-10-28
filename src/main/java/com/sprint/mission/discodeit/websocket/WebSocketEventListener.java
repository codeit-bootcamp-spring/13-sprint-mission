package com.sprint.mission.discodeit.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;


@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final WebSocketRegistry webSocketRegistry;

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {

        Optional.ofNullable(webSocketRegistry.joinedChannelId(event.getSessionId()))
                .ifPresent(channelId-> log.debug("[WebSocket] {} 채널 구독 해제 - session: {}"
                        ,channelId, event.getSessionId()));

        webSocketRegistry.unregister(event.getSessionId());
        log.debug("[WebSocket] {} 세션 연결 끊김", event.getSessionId());
    }

}
