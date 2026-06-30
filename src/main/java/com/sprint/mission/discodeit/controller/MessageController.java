package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    // 1. 메세지 발송
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> createMessage(
            @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {

        MessageResponse response = messageService.create(messageCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 2. 채널의 메세지 목록 조회
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages(
            @RequestParam("channelId") UUID channelId) {

        List<MessageResponse> responses = messageService.findAllByChannelId(channelId);
        return ResponseEntity.ok(responses);
    }

    // 3. 메세지 내용 수정
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable("messageId") UUID messageId,
            @RequestBody MessageUpdateRequest messageUpdateRequest) {

        MessageResponse response = messageService.update(messageId, messageUpdateRequest);
        return ResponseEntity.ok(response);
    }

    // 4. 메세지 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable("messageId") UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

}
