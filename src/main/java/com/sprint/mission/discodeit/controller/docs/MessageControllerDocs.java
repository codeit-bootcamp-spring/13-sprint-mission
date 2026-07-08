package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Message", description = "Message API")
public interface MessageControllerDocs {


    @Operation(summary = "메시지 생성 API")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<MessageDto> createMessage(
            @RequestPart MessageCreateRequest messageCreateRequest,
            @RequestPart(required = false)List<MultipartFile> attachments);


    @Operation(summary = "메시지 수정 API")
    @PatchMapping("/{messageId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<MessageDto> updateMessage(
            @PathVariable UUID messageId, @RequestBody MessageUpdateRequest request);

    @Operation(summary = "메시지 삭제 API")
    @DeleteMapping("/{messageId}")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId);

    @Operation(summary = "특정 채널 메시지 목록 조회 API")
    @GetMapping()
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않음")
    })
    public ResponseEntity<PageResponse<MessageDto>> getAllMessages(@RequestParam UUID channelId,
                                                                   @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable);

}
