package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public ResponseEntity<MessageDto> create(@Valid @RequestBody MessageCreateRequest request) {
        log.info("메시지 생성 요청: channelId={}, userId={}", request.channelId(), request.userId());
        return ResponseEntity.ok(messageService.create(request));
    }

    @GetMapping
    public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
            @RequestParam UUID channelId,
            @RequestParam(required = false) Instant cursor,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(messageService.findAllByChannelId(channelId, cursor, pageable));
    }

    @PatchMapping(value = "/{messageId}")
    public ResponseEntity<MessageDto> update(@PathVariable UUID messageId, @Valid @RequestBody MessageUpdateRequest request) {
        log.info("메시지 수정 요청: messageId={}", messageId);
        return ResponseEntity.ok(messageService.update(request));
    }

    @DeleteMapping(value = "/{messageId}")
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        log.info("메시지 삭제 요청: messageId={}", messageId);
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }
}
