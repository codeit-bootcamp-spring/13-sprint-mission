package com.sprint.mission.discodeit.security;

import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("messageSecurity")
@RequiredArgsConstructor
public class MessageSecurity {

    private final MessageRepository messageRepository;


    public boolean isAuthor(UUID messageId, DiscodeitUserDetails principal) {
        return messageRepository.findById(messageId)
                .map(message -> message.getAuthor() != null
                && message.getAuthor().getId().equals(principal.getUserDto().id()))
                .orElse(true);
    }
}
