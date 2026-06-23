package com.sprint.mission.discodeit.controller;


import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.message.MessageResponse;
import com.sprint.mission.discodeit.dto.message.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;


    //message 보내기 api
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<MessageResponse> createMessage(@RequestBody MessageCreateRequest request) {
        MessageResponse messageResponse = messageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(messageResponse);
    }

    // message 수정 api
    @RequestMapping(value = "/{messageid}", method = RequestMethod.PATCH)
    public ResponseEntity<MessageResponse> updateMessage(@PathVariable UUID messageid, @RequestBody MessageUpdateRequest request) {
        MessageResponse messageResponse = messageService.updateMessage(messageid, request);
        return ResponseEntity.status(HttpStatus.OK).body(messageResponse);
    }

    // message 삭제 api
    @RequestMapping(value = "/{messageid}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID messageid) {
        messageService.delete(messageid);
        return ResponseEntity.noContent().build();
    }

    // 특정 채널 message목록 조회 api
    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<List<MessageResponse>> getAllMessages(@RequestParam UUID channelid) {
        List<MessageResponse> allByChannelId = messageService.findAllByChannelId(channelid);
        return ResponseEntity.status(HttpStatus.OK).body(allByChannelId);
    }


}