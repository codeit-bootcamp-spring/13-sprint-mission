package com.sprint.mission.discodeit.controller.docs;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<MessageResponse> createMessage(
            @RequestPart MessageCreateRequest messageCreateRequest,
            @RequestPart(required = false)List<MultipartFile> attachments);


    @Operation(summary = "메시지 수정 API")
    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageResponse> updateMessage(
            @PathVariable UUID messageId, @RequestBody MessageUpdateRequest request);

    @Operation(summary = "메시지 삭제 API")
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId);

    @Operation(summary = "특정 채널 메시지 목록 조회 API")
    @GetMapping()
    public ResponseEntity<List<MessageResponse>> getAllMessages(@RequestParam UUID channelId);

}
