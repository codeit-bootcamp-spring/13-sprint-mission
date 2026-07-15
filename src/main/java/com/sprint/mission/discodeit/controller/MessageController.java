package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.controller.api.MessageApi;
import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.sprint.mission.discodeit.dto.response.PageResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
public class MessageController implements MessageApi {

  private final MessageService messageService;

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

  @PatchMapping(path = "{messageId}")
  public ResponseEntity<Message> update(
          @PathVariable("messageId") UUID messageId,
          @RequestBody MessageUpdateRequest request) {
    // Request DTO에서 값을 꺼내서 Service에 전달
    Message updatedMessage = messageService.update(
            messageId,
            request.newContent()
    );
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(updatedMessage);
  }

  @DeleteMapping(path = "{messageId}")
  public ResponseEntity<Void> delete(@PathVariable("messageId") UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
  }

  @Operation(summary = "Channel의 Message 목록 조회")
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "200", description = "Message 목록 조회 성공",
                  content = @Content(schema = @Schema(implementation = PageResponse.class))
          )
  })
  @GetMapping
  public ResponseEntity<PageResponse<Message>> findAllByChannelId(
          @RequestParam("channelId") UUID channelId,
          @RequestParam(value = "page", defaultValue = "0") int page
  ) {
    Pageable pageable = PageRequest.of(page, 50, Sort.by(Sort.Direction.DESC, "createdAt"));
    PageResponse<Message> response = messageService.findAllByChannelId(channelId, pageable);
    return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
  }
}