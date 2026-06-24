package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Controller
@RequestMapping("/api/messages")
@ResponseBody
public class MessageController {

    private final MessageService messageService;

    // 메시지 전송
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> create (@Valid @RequestBody MessageCreateRequest request){
        MessageResponse message = messageService.create(request);
        URI location = URI.create("/api/messages/" + message.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    // 메시지 수정
    @RequestMapping(path = "/{messageId}", method = RequestMethod.PATCH)
    public ResponseEntity<MessageResponse> update(@PathVariable UUID messageId,
                                                  @Valid @RequestBody MessageUpdateRequest request){
        MessageResponse updatedMessage = messageService.update(messageId, request);
        return ResponseEntity.status(HttpStatus.OK).body(updatedMessage);
    }

    // 메시지 삭제
    @RequestMapping(path = "/{messageId}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> delete(@PathVariable UUID messageId){
        messageService.delete(messageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // 특정 채널의 메시지 목록 조회
    @RequestMapping(path = "/channels/{channelId}", method = RequestMethod.GET)
    public ResponseEntity<List<MessageResponse>> findAllByChannelId(@PathVariable UUID channelId){
        List<MessageResponse> foundMessages = messageService.findAllByChannelId(channelId);
        return ResponseEntity.status(HttpStatus.OK).body(foundMessages);
    }
}
