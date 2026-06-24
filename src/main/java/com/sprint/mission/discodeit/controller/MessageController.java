package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.exception.DiscodeitException;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;

    // 1. 메세지 발송
    @RequestMapping(method = RequestMethod.POST)
    public MessageResponse createMessage(@RequestBody MessageCreateRequest messageCreateRequest) {
        return messageService.create(messageCreateRequest);
    }
/*
    // 2. 특정 메세지 조회 -> ?
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public MessageResponse getMessage(@PathVariable UUID id) {
        return messageService.findById(id)
                .orElseThrow(() -> new DiscodeitException.MessageNotFoundException
                        ("해당 메세지를 찾을 수 없습니다."));
    }
 */

    // 3. 전체 메세지 조회 -> ?
    @RequestMapping(method = RequestMethod.GET)
    public List<MessageResponse> getAllMessages(@RequestParam(required = false) UUID id) {
        if (id != null) {
            // [ ] 특정 채널의 메시지 목록을 조회할 수 있다.
            return messageService.findAllByChannelId(id);
        }
        return messageService.findAll(null);
    }

    // 4. 메세지 내용 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public MessageResponse updateMessage
    (@PathVariable UUID id,
     @RequestBody MessageUpdateRequest messageUpdateRequest) {
        return messageService.update(id, messageUpdateRequest);
    }

    // 5. 메세지 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteMessage(@PathVariable UUID id) {
        messageService.delete(id);
    }

}
