package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.request.MessageMultipartRequest;
import com.sprint.mission.discodeit.dto.MessagePageRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.mapper.MessageMultipartMapper;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final MessageMultipartMapper messageMultipartMapper;
    private final Validator validator;

    // 메시지 생성 - JSON 요청
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> create(
            @Valid @RequestBody MessageCreateRequest request
    ) {
        MessageResponse response = messageService.create(request);

        return created(response);
    }

    // 메시지 생성 - multipart/form-data 요청
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> createWithMultipart(
            @ModelAttribute MessageMultipartRequest multipartRequest
    ) throws IOException {
        MessageCreateRequest request =
                messageMultipartMapper.toCreateRequest(multipartRequest);

        var violations = validator.validate(request);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        MessageResponse response = messageService.create(request);

        return created(response);
    }

    // 메시지 단건 조회
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> read(
            @PathVariable UUID messageId
    ) {
        return ResponseEntity.ok(
                messageService.read(messageId)
        );
    }

    // 특정 채널의 메시지 목록 조회 - 커서 페이지네이션
    @GetMapping
    public ResponseEntity<PageResponse<MessageResponse>> findAllByChannelId(
            @Valid @ModelAttribute MessagePageRequest request
    ) {
        return ResponseEntity.ok(
                messageService.findAllByChannelId(
                        request.getChannelId(),
                        request.getActualCursor(),
                        request.getSize()
                )
        );
    }

    // 메시지 수정
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> update(
            @PathVariable UUID messageId,
            @Valid @RequestBody MessageUpdateRequest request
    ) {
        MessageUpdateRequest fixedRequest =
                new MessageUpdateRequest(
                        messageId,
                        request.getContent()
                );

        return ResponseEntity.ok(
                messageService.update(fixedRequest)
        );
    }

    // 메시지 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID messageId
    ) {
        messageService.delete(messageId);

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<MessageResponse> created(
            MessageResponse response
    ) {
        URI location =
                URI.create("/api/messages/" + response.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }
}