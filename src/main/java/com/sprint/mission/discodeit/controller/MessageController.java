package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.BinaryContentCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.MessageUpdateRequest;
import com.sprint.mission.discodeit.service.MessageService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Controller
@ResponseBody
@RequestMapping("/api/messages")
public class MessageController {

  private final MessageService messageService;

  // 메시지 생성
  @RequestMapping(method = RequestMethod.POST)
  public Object createMessage(
      @RequestPart("messageCreateRequest") MessageCreateRequest request,
      @RequestPart(value = "attachments", required = false)
      List<MultipartFile> attachments
  ) throws IOException {

    List<BinaryContentCreateRequest> attachmentRequests = new ArrayList<>();

    if (attachments != null) {
      for (MultipartFile file : attachments) {
        if (!file.isEmpty()) {
          attachmentRequests.add(
              new BinaryContentCreateRequest(
                  file.getOriginalFilename(),
                  file.getContentType(),
                  file.getBytes()
              )
          );
        }
      }
    }

    return messageService.create(
        request,
        attachmentRequests
    );
  }

  // 특정 채널의 메시지 조회
  @RequestMapping(method = RequestMethod.GET)
  public Object getMessagesByChannel(
      @RequestParam UUID channelId
  ) {
    return messageService.findAllByChannelId(channelId);
  }

  // 메시지 수정
  @RequestMapping(
      value = "/{messageId}",
      method = RequestMethod.PATCH
  )
  public Object updateMessage(
      @PathVariable UUID messageId,
      @RequestBody MessageUpdateRequest request
  ) {

    return messageService.update(
        messageId,
        request
    );
  }

  // 메시지 삭제
  @RequestMapping(
      value = "/{messageId}",
      method = RequestMethod.DELETE
  )
  public void deleteMessage(
      @PathVariable UUID messageId
  ) {

    messageService.delete(messageId);
  }
}