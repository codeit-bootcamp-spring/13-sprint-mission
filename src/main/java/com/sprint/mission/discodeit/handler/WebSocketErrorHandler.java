package com.sprint.mission.discodeit.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompConversionException;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

@Slf4j
@Component
public class WebSocketErrorHandler extends StompSubProtocolErrorHandler {

    @Override
    public Message<byte[]> handleClientMessageProcessingError(Message<byte[]> clientMessage, Throwable ex) {
        Throwable cause = (ex instanceof MessageDeliveryException && ex.getCause() != null) ? ex.getCause() : ex;
        String reason;
        if (cause instanceof AuthenticationException || cause instanceof AccessDeniedException) {
            reason = cause.getMessage();
        } else if (cause instanceof StompConversionException) {
            reason = "메시지가 너무 큽니다";   // StompConfig 의 크기 제한(8KB)을 넘었다
        } else {
            reason = "요청을 처리할 수 없습니다";
            // 밖으로는 덮고, 원인은 우리 로그에 남긴다(HTTP 500 에서 스택을 응답엔 숨기고 로그엔 남기듯)
            log.warn("[STOMP] 거절 — {} · 원인", reason, cause);
            return errorFrame(reason);
        }
        log.warn("[STOMP] 거절 — {}", reason);
        return errorFrame(reason);
    }

    private Message<byte[]> errorFrame(String reason) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.ERROR);
        accessor.setMessage(reason);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

}