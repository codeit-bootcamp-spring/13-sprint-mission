package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.*;
import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.service.*;
import lombok.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

import java.io.*;
import java.util.*;

@RequestMapping("/api/messages")
@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping
    public MessageResponse create(@RequestPart MessageRequest.Create request,
                                  @RequestPart(required = false) List<MultipartFile> files) {
        List<CreateBinaryContentRequest> binaryRequests = new ArrayList<>();

        if (files != null && !files.isEmpty()) {
            binaryRequests = files.stream()
                    .map(file -> {
                        try {
                            return new CreateBinaryContentRequest(
                                    file.getOriginalFilename(),
                                    file.getContentType(),
                                    file.getBytes()
                            );
                        } catch (IOException e) {
                            throw new UncheckedIOException("파일 데이터를 읽는 중 오류가 발생했습니다.", e);
                        }
                    })
                    .toList();
        }

        return messageService.create(request, binaryRequests);
    }

    @PatchMapping(value = ("/{messageId}"))
    public MessageResponse update(@PathVariable UUID messageId,
                                  @RequestBody MessageRequest.Update request) {
        return messageService.update(messageId, request);
    }

    @DeleteMapping(value = ("/{messageId}"))
    public void delete(@PathVariable UUID messageId) {
        messageService.delete(messageId);
    }

    @GetMapping
    public ResponseEntity<PageResponse<MessageResponse>> getMessages(
            @RequestParam UUID channelId,
            @RequestParam(defaultValue = "50") int page) {
        PageResponse<MessageResponse> response = messageService.getMessages(channelId, page);
        return ResponseEntity.ok(response);
    }

}
