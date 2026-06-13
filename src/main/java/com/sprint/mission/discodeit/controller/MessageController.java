package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.dto.response.MessageUpdateResponse;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    //메시지 생성
    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<Void> createMessage(@Valid @RequestBody MessageCreateRequest request) {
        messageService.createMessage(request);

        return ResponseEntity.ok().build();
    }

    //특정 채널의 메시지 목록 조회
    //스프린트 3에서 DTO로 묶으라는 지시가 없어서 생List로 반환
    @RequestMapping(value = "/{channelId}", method = RequestMethod.GET)
    public List<Message> findMessagesByChannel(@PathVariable UUID channelId) {
        List<Message> responseList = messageService.findAllByChannelId(channelId);

        return responseList;
    }

    //메시지 수정
    @RequestMapping(value = "/{id}", method = RequestMethod.PATCH)
    public ResponseEntity<MessageUpdateResponse> updateMessage(@PathVariable UUID id, @Valid @RequestBody MessageUpdateRequest request) {
        MessageUpdateResponse response = messageService.updateMessage(request);

        return ResponseEntity.ok().body(response);
    }

    //메시지 삭제
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseEntity<Void> deleteMessage(@PathVariable UUID id) {
         messageService.deleteMessage(id);

        return ResponseEntity.noContent().build();
    }

}
