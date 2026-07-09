package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageResponse> create(
      @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {

    MessageResponse response = messageService.create(request, attachments);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageResponse> update(
      @PathVariable("messageId") UUID id,
      @RequestBody MessageUpdateRequest request) {
    MessageResponse update = messageService.update(id, request);

    return ResponseEntity.ok(update);
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable("messageId") UUID id) {
    messageService.delete(id);

    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public ResponseEntity<List<MessageResponse>> findAllByChannelId(@RequestParam UUID channelId) {
    List<MessageResponse> allByChannelId = messageService.findAllByChannelId(channelId);

    return ResponseEntity.ok(allByChannelId);
  }

}
