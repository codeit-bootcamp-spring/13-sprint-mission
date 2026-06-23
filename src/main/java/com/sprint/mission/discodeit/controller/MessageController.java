package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // 메시지 생성
    @RequestMapping(method = RequestMethod.POST)
    public MessageResponse create(@RequestBody CreateMessageRequest request) {
        CreateMessageRequest messageRequest =
                new CreateMessageRequest(
                        request.getContent(),
                        request.getChannelId(),
                        request.getAuthorId()
                );

        return messageService.create(
                messageRequest
        );
    }

    // 메시지 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public MessageResponse update(@PathVariable UUID id, @RequestBody UpdateMessageRequest request) {
        return messageService.update(id, request);
    }

    // 메시지 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID id) {
        messageService.delete(id);
    }

    // 메시지 목록 조회
    @RequestMapping(method = RequestMethod.GET)
    public List<MessageResponse> findAllByChannelId(@RequestParam UUID channelId) {
        return messageService.findAllByChannelId(channelId);
    }


}
