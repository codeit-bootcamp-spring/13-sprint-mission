package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.CreateMessageRequest;
import com.sprint.mission.discodeit.dto.UpdateMessageRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.dto.response.PageResponse;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/messages")
@RequiredArgsConstructor
public class MessageController {

    private static final Logger log =
            LoggerFactory.getLogger(MessageController.class);

    private final MessageService messageService;

    @PostMapping
    public MessageDto create(
            @Valid @RequestBody CreateMessageRequest request
    ) {
        log.debug(
                "메시지 생성 요청: channelId={}, userId={}",
                request.getChannelId(),
                request.getUserId()
        );

        MessageDto messageDto = messageService.create(request);

        log.info("메시지 생성 완료");

        return messageDto;
    }

    @GetMapping
    public PageResponse<MessageDto> findAll(
            @RequestParam UUID channelId,
            @RequestParam(defaultValue = "0") int page
    ) {
        log.debug(
                "메시지 목록 조회 요청: channelId={}, page={}",
                channelId,
                page
        );

        PageResponse<MessageDto> response =
                messageService.findAllByChannelId(channelId, page);

        log.debug("메시지 목록 조회 완료");

        return response;
    }

    @GetMapping("/{id}")
    public MessageDto find(
            @PathVariable UUID id
    ) {
        log.debug("메시지 조회 요청: messageId={}", id);

        return messageService.find(id);
    }

    @PutMapping("/{id}")
    public MessageDto update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMessageRequest request
    ) {
        log.debug("메시지 수정 요청: messageId={}", id);

        MessageDto messageDto = messageService.update(id, request);

        log.info("메시지 수정 완료: messageId={}", id);

        return messageDto;
    }

    @DeleteMapping("/{id}")
    public void delete(
            @PathVariable UUID id
    ) {
        log.debug("메시지 삭제 요청: messageId={}", id);

        messageService.delete(id);

        log.info("메시지 삭제 완료: messageId={}", id);
    }
}