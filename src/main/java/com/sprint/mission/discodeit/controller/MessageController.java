package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Tag(name = "Message", description = "Message 관련 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  // GET /api/messages?channelId= -> Channel의 Message 목록 조회
  @GetMapping
  public ResponseEntity<List<Message>> findAllByChannelId(
          @RequestParam("channelId") UUID channelId) {
    List<Message> messages = messageService.findAllByChannelId(channelId);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(messages);
  }

  // POST /api/messages -> Message 생성 (첨부파일 포함 가능)
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Message> create(
          @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
          @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {
    List<BinaryContentCreateRequest> attachmentRequests = Optional.ofNullable(attachments)
            .map(files -> files.stream()
                    .map(file -> {
                      try {
                        return new BinaryContentCreateRequest(
                                file.getOriginalFilename(),
                                file.getContentType(),
                                file.getBytes()
                        );
                      } catch (IOException e) {
                        throw new RuntimeException(e);
                      }
                    })
                    .toList())
            .orElse(new ArrayList<>());
    Message createdMessage = messageService.create(messageCreateRequest, attachmentRequests);
    return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(createdMessage);
  }

  // PATCH /api/messages/{messageId} -> Message 내용 수정
  @PatchMapping("/{messageId}")
  public ResponseEntity<Message> update(
          @PathVariable("messageId") UUID messageId,
          @RequestBody MessageUpdateRequest request
  ) {
    Message updatedMessage = messageService.update(messageId, request);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedMessage);
  }

  // DELETE /api/messages/{messageId} -> Message 삭제
  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable("messageId") UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }
}