package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    @RequestMapping( method = RequestMethod.POST)
    public ResponseEntity<Message> create(@RequestBody MessageCreateRequest messageCreateRequest){
        Message message = messageService.create(messageCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
    public ResponseEntity<Message> find(@PathVariable UUID messageId){
        Message findMessage = messageService.find(messageId);
        return ResponseEntity.status(HttpStatus.OK).body(findMessage);
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public ResponseEntity<List<Message>> findAllByChannelId(@RequestParam UUID channelId){
        List<Message> allMessageByChannel = messageService.findAllByChannelId(channelId);
        return ResponseEntity.status(HttpStatus.OK).body(allMessageByChannel);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<Message> update(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest messageUpdateRequest){
        Message updateMessage = messageService.update(messageId, messageUpdateRequest);
        return ResponseEntity.status(HttpStatus.OK).body(updateMessage);
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID messageId){
        messageService.delete(messageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
