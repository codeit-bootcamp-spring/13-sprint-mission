package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Message", description = "Message API")
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;


    @Operation(summary = "메시지 생성 API")
    @PostMapping()
    public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageCreateRequest request) {
        MessageResponse messageResponse = messageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }


    @Operation(summary = "메시지 수정 API")
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest request) {
        MessageResponse messageResponse = messageService.updateMessage(messageId, request);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    @Operation(summary = "메시지 삭제 API")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "특정 채널 메시지 목록 조회 API")
    @GetMapping()
    public ResponseEntity<List<MessageResponse>> getAllMessages(@RequestParam UUID channelId) {
        List<MessageResponse> allByChannelId = messageService.findAllByChannelId(channelId);
        return ResponseEntity.status(HttpStatus.OK).body(allByChannelId);
    }

}