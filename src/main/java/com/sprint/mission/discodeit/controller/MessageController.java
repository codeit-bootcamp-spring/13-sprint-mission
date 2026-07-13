package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.controller.docs.MessageControllerDocs;
import com.sprint.mission.discodeit.dto.command.binarycontent.BinaryContentCreateCommand;
import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageDto;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController implements MessageControllerDocs {

    private final MessageService messageService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDto> createMessage(@RequestPart("messageCreateRequest") MessageCreateRequest request,
                                                    @RequestPart(required = false) List<MultipartFile> attachments) {
        List<BinaryContentCreateCommand> attachmentRequests = attachments == null ?
                new ArrayList<>() :
                attachments.stream().map(FileUtils::toCommand)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        MessageDto messageDto = messageService.create(request.toCommand(), attachmentRequests);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageDto);
    }


    @PatchMapping("/{messageId}")
    public ResponseEntity<MessageDto> updateMessage(@PathVariable UUID messageId, @RequestBody MessageUpdateRequest request) {
        MessageDto messageDto = messageService.updateMessage(messageId, request.toCommand());
        return ResponseEntity.status(HttpStatus.OK).body(messageDto);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping()
    public ResponseEntity<PageResponse<MessageDto>> getAllMessages(@RequestParam UUID channelId,
                                                                   @RequestParam(required = false) Instant cursor,
                                                                   @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        PageResponse<MessageDto> allByChannelId = messageService.findAllByChannelIdWithCursor(channelId, cursor, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(allByChannelId);
    }

}