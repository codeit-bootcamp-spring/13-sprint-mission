package com.sprint.mission.discodeit.controller.api;

import com.sprint.mission.discodeit.dto.request.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.entity.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Message", description = "Message API")
@RequestMapping("/api/messages")
public interface MessageApi {

  @Operation(summary = "Message 생성")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  ResponseEntity<Message> create(
      @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @RequestPart(value = "attachments", required = false)
      List<MultipartFile> attachments
  );

  @Operation(summary = "Message 내용 수정")
  @PostMapping("/message")
  ResponseEntity<Message> update(
      @PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request
  );

  @Operation(summary = "Message 삭제")
  @DeleteMapping("/{message}")
  ResponseEntity<Void> delete(
      @PathVariable("messageId") UUID messageId
  );

  @Operation(summary = "Channel의 Message 목록 조회")
  @GetMapping
  ResponseEntity<List<Message>> findAllByChannelId(
      @RequestParam UUID channelId
  );

}
