package com.sprint.mission.discodeit.websocket;

import com.sprint.mission.discodeit.dto.request.CreateMessageRequest;
import com.sprint.mission.discodeit.security.ChannelSecurity;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class MessageWebSocketController {

    private final MessageService messageService;
    private final ChannelSecurity channelSecurity;

    @MessageMapping("/messages")
    public void create(@Valid @Payload CreateMessageRequest request,
                       Principal principal) {

        if(!(principal instanceof Authentication authentication)
        || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("인증이 필요합니다.");
        }

        if (!(authentication.getPrincipal()
        instanceof DiscodeitUserDetails userDetails)) {
            throw new AccessDeniedException("사용자의 정보를 확인할 수 없습니다.");
        }

        UUID authenticatedUserId = userDetails.getUserDto().id();
        if(!authenticatedUserId.equals(request.authorId())) {
            throw new AccessDeniedException("다른 사용자의 명의로 메세지를 작성할 수 없습니다.");
        }

        if (!channelSecurity.canAccess(
                request.channelId(),
                authentication)) {
            throw new AccessDeniedException("해당 채널에 메시지를 작성할 권한이 없습니다.");
        }

        messageService.create(
                request.toCommand(),
                List.of()
        );
    }


}
