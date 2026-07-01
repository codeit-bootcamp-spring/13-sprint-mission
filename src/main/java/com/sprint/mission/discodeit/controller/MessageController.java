package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RequestMapping("/api/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public MessageResponse create(@RequestBody MessageRequest.CreateMessageRequest request) {
        return messageService.create(request);
    }

    @RequestMapping(value = ("/{messageId}"), method = RequestMethod.PATCH)
    public MessageResponse update(@PathVariable UUID messageId,
                                  @RequestBody MessageRequest.UpdateMessageRequest request) {
        return messageService.update(messageId, request);
    }

    @RequestMapping(value = ("/{messageId}"), method = RequestMethod.DELETE)
    public void delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<MessageResponse> findAll(
            @RequestParam UUID channelId
    ) {
        return messageService.findAllByChannelId(channelId);
    }

}
