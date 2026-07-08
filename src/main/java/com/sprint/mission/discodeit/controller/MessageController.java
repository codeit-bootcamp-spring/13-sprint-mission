package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  @RequestMapping(method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<MessageDto> create(
      @RequestPart("messageCreateRequest") @Valid MessageCreateRequest messageCreateRequest) {
    MessageDto message = messageService.create(messageCreateRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(message);
  }


  @RequestMapping(value = "/{messageId}", method = RequestMethod.GET)
  public ResponseEntity<MessageDto> find(@PathVariable UUID messageId) {
    MessageDto findMessage = messageService.find(messageId);
    return ResponseEntity.status(HttpStatus.OK).body(findMessage);
  }

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<List<MessageDto>> findAllByChannelId(@RequestParam UUID channelId) {
    List<MessageDto> allMessageByChannel = messageService.findAllByChannelId(channelId);
    return ResponseEntity.status(HttpStatus.OK).body(allMessageByChannel);
  }

  @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
  public ResponseEntity<MessageDto> update(@PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest messageUpdateRequest) {
    MessageDto updateMessage = messageService.update(messageId, messageUpdateRequest);
    return ResponseEntity.status(HttpStatus.OK).body(updateMessage);
  }

  @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

}
