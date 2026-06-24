package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/v1/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public MessageResponse create(@RequestBody MessageRequest.CreateMessageRequest request) {
        return messageService.create(request);
    }

    @RequestMapping(value = ("/{messageId}"), method = RequestMethod.PUT)
    public MessageResponse update(@PathVariable UUID messageId,
                                  @RequestBody MessageRequest.UpdateMessageRequest request) {
        return messageService.update(messageId, request);
    }

    @RequestMapping(value = ("/{messageId}"), method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }

    @RequestMapping(value = "/channel/{channelId}", method = RequestMethod.GET)
    public List<MessageResponse> findAll(
            @PathVariable UUID channelId
    ) {
        return messageService.findAllByChannelId(channelId);
    }

}
