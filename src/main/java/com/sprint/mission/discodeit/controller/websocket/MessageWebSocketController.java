package com.sprint.mission.discodeit.controller.websocket;


import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;


    /**
     * 첨부 파일이 없는 매서드 라면, 웹 소켓으로 메세지를 생성하는 매서드.
     * @param request
     */
    @MessageMapping("/pub/messages")
    public void textMessage(
            MessageCreateRequest request
    ){
        messageService.createMessage(request,null);
    }

}
