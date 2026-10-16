package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.request.MessageCreateRequest;
import com.sprint.mission.discodeit.dto.request.StompMessageCreateRequest;
import com.sprint.mission.discodeit.dto.response.MessageDto;
import com.sprint.mission.discodeit.security.ChannelSecurity;
import com.sprint.mission.discodeit.security.CustomUserDetails;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class StompMessageController {

  private final MessageService messageService;
  private final SimpMessagingTemplate messagingTemplate;
  private final ChannelSecurity channelSecurity;

  @MessageMapping("/messages")
  public void create(
      @Payload @Valid StompMessageCreateRequest request, Principal principal
  ) {
    if (!(principal instanceof Authentication authentication)
        || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
      throw new AccessDeniedException("인증이 필요합니다.");
    }

    if (!channelSecurity.canAccess(
        request.channelId(), userDetails.getUserDto().id()
    )) {
      throw new AccessDeniedException("접근할 수 없는 채널입니다.");
    }

    MessageCreateRequest createRequest = new MessageCreateRequest(
        request.content(),
        request.channelId(),
        userDetails.getUserDto().id()
    );

    MessageDto message = messageService.create(createRequest, null);

    messagingTemplate.convertAndSend("/sub/channels." + request.channelId() + ".messages", message);
  }
}
