package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    // 1. 메시지 보내기 (기본적으로 첨부파일 리스트는 빈 배열로 유연하게 처리 가능하도록 유도)
    @RequestMapping(method = RequestMethod.POST)
    public Message createMessage(@RequestBody MessageCreateRequest request) {
        return messageService.create(request, new ArrayList<>());
    }

    // 2. 메시지 수정
    @RequestMapping(value = "/{messageId}", method = RequestMethod.PUT)
    public Message updateMessage(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest request) {
        return messageService.update(messageId, request);
    }

    // 3. 메시지 삭제
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public void deleteMessage(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }

    // 4. 특정 채널의 메시지 목록 조회 (쿼리 파라미터 ?channelId=... 사용)
    @RequestMapping(method = RequestMethod.GET)
    public List<Message> getMessagesByChannelId(@RequestParam UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }
}