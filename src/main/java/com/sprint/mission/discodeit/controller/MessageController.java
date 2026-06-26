package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.MessageControllerDocs;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController implements MessageControllerDocs {

    private final MessageService messageService;


    @PostMapping()
    public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageCreateRequest request) {
        MessageResponse messageResponse = messageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }


    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest request) {
        MessageResponse messageResponse = messageService.updateMessage(messageId, request);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<List<MessageResponse>> getAllMessages(@RequestParam UUID channelId) {
        List<MessageResponse> allByChannelId = messageService.findAllByChannelId(channelId);
        return ResponseEntity.status(HttpStatus.OK).body(allByChannelId);
    }

}