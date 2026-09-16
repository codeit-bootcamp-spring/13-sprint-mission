package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component("messageSecurity")
@RequiredArgsConstructor
public class MessageSecurity {

    private final MessageRepository messageRepository;

    @Transactional(readOnly = true)
    public boolean isAuthor(UUID messageId, Authentication authentication) {
        if (messageId == null || authentication == null) {
            return false;
        }

        if(!(authentication.getPrincipal() instanceof  DiscodeitUserDetails userDetails)) {
            return false;
        }

        Optional<Message> message = messageRepository.findById(messageId);

        if(message.isEmpty()) {
            return true;
        }

        UUID loginUserId = userDetails.getUserDto().id();
        UUID authorId = message.get().getAuthor().getId();

        return loginUserId.equals(authorId);
    }
}
