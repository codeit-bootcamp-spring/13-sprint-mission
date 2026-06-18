package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> create(
            @RequestBody MessageCreateRequest request
    ) {
        return ResponseEntity.ok(messageService.create(request));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<Collection<MessageResponse>> findAllByChannelId(
            @RequestParam UUID channelId
    ) {
        return ResponseEntity.ok(messageService.findAllByChannelId(channelId));
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<MessageResponse> update(
            @RequestBody MessageUpdateRequest request
    ) {
        return ResponseEntity.ok(messageService.update(request));
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID id
    ) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
