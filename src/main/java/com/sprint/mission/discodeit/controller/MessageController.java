package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.binarycontent.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/messages")
@Tag(name = "Message", description = "Message API") // 그룹 묶기
public class MessageController {

  private final MessageService messageService;

  // 메시지 전송
  @Operation(summary = "Message 생성") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "Message가 성공적으로 생성됨"),
      @ApiResponse(responseCode = "404", description = "Channel 또는 User를 찾을 수 없음")
  })
  @RequestMapping(
      method = RequestMethod.POST,
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE
  )
  public ResponseEntity<MessageDto> create(
      @Valid @RequestPart("messageCreateRequest") MessageCreateRequest messageCreateRequest,
      @Parameter(description = "Message 첨부 파일들")
      @ArraySchema(schema = @Schema(type = "string", format = "binary"))
      @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
    // 여러 개의 첨부파일 선택적으로 등록하도록 한다
    List<BinaryContentCreateRequest> attachmentRequests = Optional.ofNullable(attachments)
        .map(files -> files.stream()
            .map(file -> {
              try {
                return new BinaryContentCreateRequest(file.getOriginalFilename(),
                    file.getContentType(), file.getBytes());
              } catch (IOException e) {
                log.error("첨부 파일 등록 실패", e);
                throw new IllegalArgumentException(e);
              }
            }).toList()).orElse(new ArrayList<>());
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(messageService.create(messageCreateRequest, attachmentRequests));
  }

  // 메시지 수정
  @Operation(summary = "Message 내용 수정") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Message가 성공적으로 수정됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음")})
  @RequestMapping(path = "/{messageId}", method = RequestMethod.PATCH)
  public ResponseEntity<MessageDto> update(
      @Parameter(name = "messageId", in = ParameterIn.PATH, description = "수정할 Message ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID messageId,
      @Valid @RequestBody MessageUpdateRequest request) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messageService.update(messageId, request));
  }

  // 메시지 삭제
  @Operation(summary = "Message 삭제") // 엔드포인트 설명
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Message가 성공적으로 삭제됨"),
      @ApiResponse(responseCode = "404", description = "Message를 찾을 수 없음")
  })
  @RequestMapping(path = "/{messageId}", method = RequestMethod.DELETE)
  public ResponseEntity<Void> delete(
      @Parameter(name = "messageId", in = ParameterIn.PATH, description = "삭제할 Message ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @PathVariable UUID messageId) {
    messageService.delete(messageId);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  // 특정 채널의 메시지 목록 조회
  @Operation(summary = "Channel의 Message 목록 조회") // 엔드포인트 설명
  @ApiResponse(
      responseCode = "200",
      description = "Message 목록 조회 성공",
      content = @Content(
          array = @ArraySchema(
              schema = @Schema(implementation = PageResponse.class))))
  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(
      @Parameter(name = "channelId", in = ParameterIn.QUERY, description = "조회할 Channel ID", required = true,
          schema = @Schema(type = "string", format = "uuid"))
      @RequestParam("channelId") UUID channelId,
      @PageableDefault(size = 50, page = 0, sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(messageService.findAllByChannelId(channelId, pageable));
  }
}
