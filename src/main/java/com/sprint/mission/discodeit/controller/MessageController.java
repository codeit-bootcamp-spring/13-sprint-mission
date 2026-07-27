package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto> create(
      @RequestPart("messageCreateRequest") @Valid MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments
  ) {

    MessageDto response = messageService.create(request, attachments);

    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PatchMapping("/{messageId}")
  public ResponseEntity<MessageDto> update(
      @PathVariable("messageId") UUID id,
      @RequestBody @Valid MessageUpdateRequest request) {
    MessageDto update = messageService.update(id, request);

    return ResponseEntity.ok(update);
  }

  @DeleteMapping("/{messageId}")
  public ResponseEntity<Void> delete(@PathVariable("messageId") UUID id) {
    messageService.delete(id);

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/{messageId}")
  public ResponseEntity<MessageDto> findById(@PathVariable("messageId") UUID id) {
    MessageDto response = messageService.findById(id);

    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @RequestParam UUID channelId,
      @PageableDefault(
          size = 50,
          sort = "createdAt",
          direction = Sort.Direction.DESC
      )
      Pageable pageable
  ) {

    PageResponse<MessageDto> response = messageService.findAllByChannelId(channelId, pageable);

    return ResponseEntity.ok(response);
  }

}
