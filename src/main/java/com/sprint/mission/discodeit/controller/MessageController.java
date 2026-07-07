package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> create(
            @RequestPart("messageCreateRequest") MessageCreateRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) {
        MessageCreateRequest createRequest = new MessageCreateRequest(
                request.content(),
                request.channelId(),
                request.authorId(),
                toBinaryContentCreateRequests(attachments)
        );

        return ResponseEntity.status(201).body(messageService.create(createRequest));
    }

    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> update(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest request
    ) {
        MessageUpdateRequest updateRequest =
                new MessageUpdateRequest(messageId, request.newContent());

        return ResponseEntity.ok(messageService.update(updateRequest));
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MessageResponse>> findAllByChannelId(@RequestParam UUID channelId) {
        return ResponseEntity.ok(messageService.findAllByChannelId(channelId));
    }

    private List<BinaryContentCreateRequest> toBinaryContentCreateRequests(List<MultipartFile> files) {
        if (files == null) {
            return List.of();
        }

        return files.stream()
                .filter(file -> !file.isEmpty())
                .map(this::toBinaryContentCreateRequest)
                .toList();
    }

    private BinaryContentCreateRequest toBinaryContentCreateRequest(MultipartFile file) {
        try {
            String contentType = file.getContentType() == null
                    ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                    : file.getContentType();

            return new BinaryContentCreateRequest(
                    file.getOriginalFilename(),
                    file.getSize(),
                    contentType,
                    file.getBytes()
            );
        } catch (IOException e) {
            throw new IllegalArgumentException("첨부 파일을 읽을 수 없습니다.", e);
        }
    }
}
