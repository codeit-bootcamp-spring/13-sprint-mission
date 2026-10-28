package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.message.MessageCreateRequest;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {
  private final MessageService messages;

  @MessageMapping("/messages")
  public void create(@Valid MessageCreateRequest request, Principal principal) {
    if (!(principal instanceof Authentication auth)
        || !(auth.getPrincipal() instanceof DiscodeitUserDetails user)
        || !user.getUserDto().id().equals(request.authorId())) {
      throw new AccessDeniedException("작성자 ID가 인증된 사용자와 다릅니다.");
    }
    messages.createMessage(request, List.of());
  }
}
