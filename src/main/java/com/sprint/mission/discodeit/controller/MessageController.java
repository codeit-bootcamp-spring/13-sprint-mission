package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.command.*;
import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import com.sprint.mission.discodeit.util.FileUtils;
import jakarta.validation.*;
import lombok.*;
import org.apache.tomcat.util.http.fileupload.*;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import java.io.*;
import java.time.*;
import java.util.*;

@RequestMapping("/api/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDto> create(@Valid @RequestPart CreateMessageRequest request,
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
