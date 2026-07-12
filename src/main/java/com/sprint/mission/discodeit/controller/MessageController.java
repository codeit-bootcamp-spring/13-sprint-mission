package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public Message create(
            @RequestBody CreateMessageRequest request
    ) {
        return messageService.create(request);
    }

    @GetMapping
    public PageResponse<MessageDto> findAll(
            @RequestParam UUID channelId,
            @RequestParam(defaultValue = "0") int page
    ) {
        return messageService.findAllByChannelId(
                channelId,
                page
        );
    }

    @GetMapping("/{id}")
    public Message find(
            @PathVariable UUID id
    ) {
        return messageService.find(id);
    }

    @PutMapping("/{id}")
    public Message update(
            @PathVariable UUID id,
            @RequestBody UpdateMessageRequest request
    ) {
        return messageService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {
        messageService.delete(id);
    }
}