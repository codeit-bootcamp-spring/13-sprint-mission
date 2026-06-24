package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@ResponseBody
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    // 메시지 생성
    @RequestMapping(method = RequestMethod.POST)
    public Object createMessage(
            @RequestBody MessageCreateRequest request
    ) {

        return messageService.create(
                request,
                Collections.emptyList()
        );
    }

    // 특정 채널의 메시지 조회
    @RequestMapping(
            value = "/channel/{channelId}",
            method = RequestMethod.GET
    )
    public Object getMessagesByChannel(
            @PathVariable UUID channelId
    ) {

        return messageService.findAllByChannelId(channelId);
    }

    // 메시지 수정
    @RequestMapping(
            value = "/{messageId}",
            method = RequestMethod.PUT
    )
    public Object updateMessage(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest request
    ) {

        return messageService.update(
                messageId,
                request
        );
    }

    // 메시지 삭제
    @RequestMapping(
            value = "/{messageId}",
            method = RequestMethod.DELETE
    )
    public void deleteMessage(
            @PathVariable UUID messageId
    ) {

        messageService.delete(messageId);
    }
}