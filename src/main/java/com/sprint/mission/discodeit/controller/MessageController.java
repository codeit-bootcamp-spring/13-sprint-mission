package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class MessageController {

    private final MessageService messageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> createMessage(
            @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {

        int attachmentCount = attachments == null ? 0 : attachments.size();
        log.debug("메시지 생성 API 요청: channelId={}, attachmentCount={}",
                messageCreateRequest.channelId(), attachmentCount);

        MessageResponse response = messageService.create(messageCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<MessageResponse>> getAllMessages(
            @RequestParam("channelId") UUID channelId,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        PageResponse<MessageResponse> responses = messageService.findAllByChannelId(channelId, page);
        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable("messageId") UUID messageId,
            @RequestBody MessageUpdateRequest messageUpdateRequest) {

        log.debug("메시지 수정 API 요청: messageId={}", messageId);
        MessageResponse response = messageService.update(messageId, messageUpdateRequest);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable("messageId") UUID messageId) {
        log.debug("메시지 삭제 API 요청: messageId={}", messageId);
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

}
