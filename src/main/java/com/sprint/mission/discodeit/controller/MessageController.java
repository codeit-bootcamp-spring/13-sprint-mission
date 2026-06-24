package com.sprint.mission.discodeit.controller;


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
public class MessageController {

    private final MessageService messageService;


    //message 보내기 api
    @PostMapping()
    public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageCreateRequest request) {
        MessageResponse messageResponse = messageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }

    // message 수정 api
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest request) {
        MessageResponse messageResponse = messageService.updateMessage(messageId, request);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    // message 삭제 api
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    // 특정 채널 message목록 조회 api
    @GetMapping()
    public ResponseEntity<List<MessageResponse>> getAllMessages(@RequestParam UUID channelId) {
        List<MessageResponse> allByChannelId = messageService.findAllByChannelId(channelId);
        return ResponseEntity.status(HttpStatus.OK).body(allByChannelId);
    }


}