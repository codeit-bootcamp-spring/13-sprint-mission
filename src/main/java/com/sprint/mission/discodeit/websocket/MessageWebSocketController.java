package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * 첨부파일 없는 텍스트 메시지를 /pub/messages에서 받는 STOMP 컨트롤러다.
 * HTTP API와 같은 요청 DTO·서비스를 사용하고 첨부파일 목록은 비워 전달한다.
 * 파일이 포함된 메시지는 기존 POST /api/messages 경로에서 처리한다.
 */
@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;

    @MessageMapping("/messages")
    public void handleMessage(@Valid @Payload MessageCreateRequest request) {
        messageService.save(request.toCommand(), List.of());
    }

}
