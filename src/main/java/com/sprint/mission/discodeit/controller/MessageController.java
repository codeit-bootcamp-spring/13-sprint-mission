package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageDto> create(
            @RequestPart("messageCreateRequest") @Valid MessageCreateRequest request,
            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
    ) throws IOException {
        log.info("Received message create request: channelId={}, userId={}", request.channelId(), request.userId());
        List<BinaryContentCreateRequest> attachmentRequests = new ArrayList<>();

        if (attachments != null) {
            for (MultipartFile attachment : attachments) {
                attachmentRequests.add(new BinaryContentCreateRequest(
                        attachment.getOriginalFilename(),
                        attachment.getContentType(),
                        attachment.getBytes()
                ));
            }
        }

        MessageCreateRequest serviceRequest = new MessageCreateRequest(
                request.content(),
                request.channelId(),
                request.userId(),
                attachmentRequests
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(messageService.create(serviceRequest));
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
            @RequestParam UUID channelId,
            @RequestParam(required = false) Instant cursor,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(messageService.findAllByChannelId(channelId, cursor, pageable));
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
    public ResponseEntity<MessageDto> find(
            @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(messageService.findById(messageId));
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<MessageDto> update(
            @PathVariable UUID messageId,
            @RequestBody @Valid MessageUpdateRequest request
    ) {
        log.info("Received message update request: messageId={}", messageId);
        return ResponseEntity.ok(messageService.update(messageId, request));
    }

    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(
            @PathVariable UUID messageId
    ) {
        log.info("Received message delete request: messageId={}", messageId);
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }
}
