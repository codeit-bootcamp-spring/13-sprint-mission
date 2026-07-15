package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.MessageResponse;
import com.sprint.mission.discodeit.dto.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.mapper.MessageMultipartMapper;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final MessageMultipartMapper messageMultipartMapper;

    // 메시지 생성 - JSON 요청 처리
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MessageResponse> create(
            @RequestBody MessageCreateRequest request
    ) {
        MessageResponse response = messageService.create(request);

        return created(response);
    }

    // 메시지 생성 - multipart/form-data 요청 처리
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse> createWithMultipart(
            @RequestParam MultiValueMap<String, String> formData,

            @RequestPart(value = "messageCreateRequest", required = false) String messageCreateRequestJson,
            @RequestPart(value = "request", required = false) String requestJson,
            @RequestPart(value = "messageRequest", required = false) String messageRequestJson,

            @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) throws IOException {
        MessageCreateRequest request = messageMultipartMapper.toCreateRequest(
                formData,
                messageCreateRequestJson,
                requestJson,
                messageRequestJson,
                attachments,
                files
        );

        MessageResponse response = messageService.create(request);

        return created(response);
    }

    // 메시지 단건 조회
    @GetMapping("/{messageId}")
    public ResponseEntity<MessageResponse> read(
            @PathVariable UUID messageId
    ) {
        MessageResponse response = messageService.read(messageId);

        return ResponseEntity.ok(response);
    }

    /*
     * 특정 채널의 메시지 목록 조회 - 커서 페이지네이션
     *
     * 첫 조회:
     * GET /api/messages?channelId=...&size=50
     *
     * 다음 조회:
     * GET /api/messages?channelId=...&cursor=2026-07-10T00:32:57.459100Z&size=50
     *
     * 호환용:
     * after 파라미터도 cursor처럼 처리한다.
     * page, sort 파라미터가 같이 와도 무시하고 cursor 방식으로 처리한다.
     */
    @GetMapping
    public ResponseEntity<PageResponse<MessageResponse>> findAllByChannelId(
            @RequestParam UUID channelId,
            @RequestParam(required = false) Instant cursor,
            @RequestParam(required = false) Instant after,
            @RequestParam(defaultValue = "50") int size,

            // 기존 프론트/요청 호환용. 커서 페이지네이션에서는 사용하지 않음.
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) String sort
    ) {
        Instant actualCursor = cursor != null ? cursor : after;

        PageResponse<MessageResponse> response =
                messageService.findAllByChannelId(
                        channelId,
                        actualCursor,
                        size
                );

        return ResponseEntity.ok(response);
    }

    // 메시지 수정
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> update(
            @PathVariable UUID messageId,
            @RequestBody MessageUpdateRequest request
    ) {
        MessageUpdateRequest fixedRequest = new MessageUpdateRequest(
                messageId,
                request.getContent()
        );

        MessageResponse response = messageService.update(fixedRequest);

        return ResponseEntity.ok(response);
    }

    // 메시지 삭제
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID messageId
    ) {
        messageService.delete(messageId);

        return ResponseEntity
                .noContent()
                .build();
    }

    private ResponseEntity<MessageResponse> created(MessageResponse response) {
        URI location = URI.create("/api/messages/" + response.getId());

        return ResponseEntity
                .created(location)
                .body(response);
    }
}