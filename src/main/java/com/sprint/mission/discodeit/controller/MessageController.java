package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
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

    // 메시지 생성
    @PostMapping
    public ResponseEntity<MessageResponse> create(@RequestBody CreateMessageRequest request) {
        MessageResponse response = messageService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 메시지 수정
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> update(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest request) {
        MessageResponse response = messageService.update(messageId, request);

        return ResponseEntity.ok(response);
    }

    // 메시지 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);

        return ResponseEntity.noContent().build();
    }

    // 메시지 목록 조회
    @GetMapping
    public ResponseEntity<List<MessageResponse>> findAllByChannelId(@RequestParam UUID channelId) {
        return ResponseEntity.ok(messageService.findAllByChannelId(channelId));
    }

}
