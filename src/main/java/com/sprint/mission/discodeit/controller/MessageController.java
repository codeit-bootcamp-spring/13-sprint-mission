package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.command.CreateBinaryContentCommand;
import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.request.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.util.FileUtils;
import io.micrometer.core.annotation.Timed;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequestMapping("/api/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Timed("message.create.async")
    public ResponseEntity<MessageDto> create(@Valid @RequestPart("messageCreateRequest") CreateMessageRequest request,
                                  @RequestPart(required = false) List<MultipartFile> files) {

        List<CreateBinaryContentCommand> file =
                files == null
                        ? List.of()
                        : files.stream()
                          .map(FileUtils::toCommand)
                          .flatMap(Optional::stream)
                          .toList();
        MessageDto dto = messageService.create(request.toCommand(),file);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @PatchMapping(value = ("/{messageId}"))
    public ResponseEntity<MessageDto> update(@PathVariable UUID messageId,
                                             @Valid @RequestBody UpdateMessageRequest request) {
        MessageDto dto = messageService.update(messageId, request.toCommand());
        return ResponseEntity.ok(dto);

    }

    @DeleteMapping(value = ("/{messageId}"))
    public ResponseEntity<Void> delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<PageResponse<MessageDto>> getMessages(
            @RequestParam UUID channelId,
            @RequestParam(required = false) Instant cursor,
            Pageable pageable) {
        PageResponse<MessageDto> response =
                messageService.getMessages(channelId, cursor, pageable);
        return ResponseEntity.ok(response);
    }

}
