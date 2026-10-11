package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.BinaryContentRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
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
            @Valid
            @RequestPart("messageCreateRequest")
            MessageCreateRequest messageCreateRequest,
            @RequestPart(
                    value = "attachments",
                    required = false
            )
            List<MultipartFile> attachments
    ) {
        List<BinaryContentRequest> attachmentRequests =
                toBinaryContentRequests(attachments);

        log.debug(
                "메시지 생성 API 요청: channelId={}, attachmentCount={}",
                messageCreateRequest.channelId(),
                attachmentRequests.size()
        );

        MessageResponse response = messageService.create(
                messageCreateRequest,
                attachmentRequests
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<MessageResponse>> getAllMessages(
            @RequestParam("channelId") UUID channelId,
            @RequestParam(
                    value = "page",
                    defaultValue = "0"
            )
            int page
    ) {
        PageResponse<MessageResponse> responses =
                messageService.findAllByChannelId(
                        channelId,
                        page
                );

        return ResponseEntity.ok(responses);
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable("messageId") UUID messageId,
            @Valid
            @RequestBody
            MessageUpdateRequest messageUpdateRequest
    ) {
        log.debug(
                "메시지 수정 API 요청: messageId={}",
                messageId
        );

        MessageResponse response = messageService.update(
                messageId,
                messageUpdateRequest
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(
            @PathVariable("messageId") UUID messageId
    ) {
        log.debug(
                "메시지 삭제 API 요청: messageId={}",
                messageId
        );

        messageService.delete(messageId);

        return ResponseEntity.noContent().build();
    }

    private List<BinaryContentRequest> toBinaryContentRequests(
            List<MultipartFile> files
    ) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        return files.stream()
                .filter(file -> !file.isEmpty())
                .map(this::toBinaryContentRequest)
                .toList();
    }

    private BinaryContentRequest toBinaryContentRequest(
            MultipartFile file
    ) {
        try {
            return new BinaryContentRequest(
                    resolveFileName(file),
                    file.getSize(),
                    resolveContentType(file),
                    file.getBytes()
            );
        } catch (IOException exception) {
            log.error(
                    "첨부파일 읽기 실패: originalFileName={}",
                    file.getOriginalFilename(),
                    exception
            );

            throw new UncheckedIOException(
                    "첨부파일을 읽을 수 없습니다.",
                    exception
            );
        }
    }

    private String resolveFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null
                || originalFileName.isBlank()) {
            return "attachment";
        }

        return originalFileName;
    }

    private String resolveContentType(MultipartFile file) {
        String contentType = file.getContentType();

        if (contentType == null) {
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }

        return contentType;
    }
}