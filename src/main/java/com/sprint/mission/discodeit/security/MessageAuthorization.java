package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("messageAuthorization")
@RequiredArgsConstructor
public class MessageAuthorization {

    private final MessageRepository messageRepository;

    public boolean isAuthor(
            UUID messageId,
            Authentication authentication
    ) {

        /*
         * null 검증은 Service에서 담당한다.
         */
        if (messageId == null) {
            return true;
        }

        Message message =
                messageRepository
                        .findById(messageId)
                        .orElse(null);

        /*
         * 메시지가 존재하지 않는 경우
         * Method Security에서 403으로 막지 않는다.
         *
         * Service의 findMessageById()에서
         * MessageNotFoundException을 발생시켜
         * 기존 404 응답을 유지한다.
         */
        if (message == null) {
            return true;
        }

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return false;
        }

        if (!(authentication.getPrincipal()
                instanceof DiscodeitUserDetails userDetails)) {

            return false;
        }

        UUID authenticatedUserId =
                userDetails
                        .getUserDto()
                        .getId();

        return authenticatedUserId.equals(
                message.getAuthorId()
        );
    }
}