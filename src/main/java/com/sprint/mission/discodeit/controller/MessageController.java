package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.entity.Message;
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

    @PostMapping
    public Message create(@RequestBody CreateMessageRequest request) {
        return messageService.create(
                new Message(
                        request.getContent(),
                        request.getUserId(),
                        request.getChannelId()
                )
        );
    }

    @GetMapping
    public List<Message> findAll() {
        return messageService.findAll();
    }

    @GetMapping("/{id}")
    public Message find(@PathVariable UUID id) {
        return messageService.find(id);
    }

    @PatchMapping("/{id}")
    public Message update(
            @PathVariable UUID id,
            @RequestBody UpdateMessageRequest request
    ) {
        return messageService.update(
                id,
                request.getContent()
        );
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        messageService.delete(id);
    }
}