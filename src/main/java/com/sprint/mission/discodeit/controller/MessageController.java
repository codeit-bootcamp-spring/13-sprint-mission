package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.swagger.MessageApi;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController implements MessageApi {

  private final MessageService messageService;

  @Override
  @Timed("message.create.async")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageResponse> createMessage(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    int attachmentCount = attachments == null ? 0 : attachments.size();
    log.debug("메시지 생성 요청: channelId={}, authorId={}, attachmentCount={}",
        request.channelId(), request.authorId(), attachmentCount);

    MessageResponse response = messageService.createMessage(request, attachments);

    log.info("메시지 생성 응답: messageId={}", response == null ? null : response.id());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @Override
  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageResponse> updateMessage(
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest request) {
    log.debug("메시지 수정 요청: messageId={}", messageId);
    MessageResponse response = messageService.updateMessage(messageId, request);

    log.info("메시지 수정 응답: messageId={}", messageId);
    return ResponseEntity.ok(response);
  }

  @Override
  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
    log.debug("메시지 삭제 요청: messageId={}", messageId);
    messageService.deleteMessage(messageId);

    log.info("메시지 삭제 응답: messageId={}", messageId);
    return ResponseEntity.noContent().build();
  }

  @Override
  @GetMapping
  public ResponseEntity<PageResponse<MessageResponse>> findMessageByChannelId(
      @RequestParam("channelId") UUID channelId,
      @RequestParam(value = "cursor", required = false) Instant cursor,
      Pageable pageable) {
    return ResponseEntity.status(HttpStatus.OK)
        .body(messageService.findAllMessageByChannelId(channelId, cursor, pageable));
  }
}
