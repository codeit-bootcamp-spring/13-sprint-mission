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
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public Message create(
            @RequestBody CreateMessageRequest request
    ) {
        return messageService.create(
                new Message(
                        request.getContent(),
                        request.getUserId(),
                        request.getChannelId()
                )
        );
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<Message> findAll() {
        return messageService.findAll();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Message find(
            @PathVariable UUID id
    ) {
        return messageService.find(id);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public Message update(
            @PathVariable UUID id,
            @RequestBody UpdateMessageRequest request
    ) {
        return messageService.update(
                id,
                request.getContent()
        );
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void delete(
            @PathVariable UUID id
    ) {
        messageService.delete(id);
    }
}