package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;


  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto> create(
      @RequestPart("messageCreateRequest") @Valid MessageCreateRequest messageCreateRequest) {
    log.debug("[메시지 생성 요청] channelId: {}", messageCreateRequest.channelId());

    MessageDto message = messageService.create(messageCreateRequest);

    log.info("[메시지 생성 완료] messageId: {}", message.id());
    return ResponseEntity.status(HttpStatus.CREATED).body(message);
  }


  @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
  public ResponseEntity<MessageDto> find(@PathVariable UUID messageId) {
    MessageDto findMessage = messageService.find(messageId);

    return ResponseEntity.status(HttpStatus.OK).body(findMessage);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @RequestParam UUID channelId, @RequestParam(required = false) Instant cursor) {
    PageResponse<MessageDto> allMessageByChannel
        = messageService.findAllByChannelId(channelId, cursor);

    return ResponseEntity.status(HttpStatus.OK).body(allMessageByChannel);
  }

  @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
  public ResponseEntity<MessageDto> update(@PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest messageUpdateRequest) {
    log.debug("[메시지 수정 요청] messageId: {}", messageId);

    MessageDto updateMessage = messageService.update(messageId, messageUpdateRequest);

    log.info("[메시지 수정 완료] messageId: {}", updateMessage.id());
    return ResponseEntity.status(HttpStatus.OK).body(updateMessage);
  }

  @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    log.debug("[메시지 삭제 요청] messageId: {}", messageId);

    messageService.delete(messageId);

    log.info("[메시지 삭제 완료] messageId: {}", messageId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}