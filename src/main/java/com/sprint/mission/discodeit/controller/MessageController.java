package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.MessageUpdateResponse;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/messages")
@Tag(name = "Message", description = "Message API")
public class MessageController {

    private final MessageService messageService;

    //메시지 생성
    @Operation(summary = "Message 생성")
    @ApiResponse(responseCode = "201", description = "Message가 성공적으로 생성됨")
    @RequestMapping(
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<MessageDto> createMessage(@Valid @RequestPart("messageCreateRequest") MessageCreateRequest request,
                                                    @RequestPart(value = "attachments", required = false) List<MultipartFile> attachments) {
        log.debug("메시지 생성 API 요청");

        MessageDto createdMessage = messageService.createMessage(request, attachments);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdMessage);
    }

    //특정 채널의 메시지 목록 조회
    @Operation(summary = "Channel의 Message 목록 조회")
    @ApiResponse(responseCode = "200", description = "Message 목록 조회 성공")
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<PageResponse<MessageDto>> findAllByChannelId(@Parameter(description = "조회할 Channel ID", required = true)
                                                                       @RequestParam UUID channelId,

                                                                       @Parameter(description = "페이징 커서 정보")
                                                                       @RequestParam(required = false) Instant cursor,

                                                                       @Parameter(description = "페이징 정보", required = true)
                                                                       @ParameterObject Pageable pageable) {
        PageResponse<MessageDto> pageResponse = messageService.getMessagesByChannelId(channelId, cursor, pageable);

        return ResponseEntity.ok().body(pageResponse);
    }

    //메시지 수정
    @Operation(summary = "Message 내용 수정")
    @ApiResponse(responseCode = "200", description = "Message가 성공적으로 수정됨")
    @RequestMapping(value = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<MessageDto> updateMessage(@Parameter(description = "수정할 Message ID", required = true)
                                                    @PathVariable UUID messageId,
                                                    @Valid @RequestBody MessageUpdateRequest request) {
        log.debug("메시지 수정 API 요청");

        MessageDto response = messageService.updateMessage(messageId, request);

        return ResponseEntity.ok().body(response);
    }

    //메시지 삭제
    @Operation(summary = "Message 삭제")
    @ApiResponse(responseCode = "204", description = "Message가 성공적으로 삭제됨")
    @RequestMapping(value = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMessage(@Parameter(description = "삭제할 Message ID", required = true)
                                              @PathVariable UUID messageId) {
        log.debug("메시지 삭제 API 요청");

        messageService.deleteMessage(messageId);

        return ResponseEntity.noContent().build();
    }

}
