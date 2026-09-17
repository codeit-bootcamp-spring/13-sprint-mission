package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageReader {
    private final MessageRepository messageRepository;

    public Optional<Message> getLatestMessageByChannelId(UUID channelId) {
        return messageRepository.findTop1ByChannel_IdOrderByCreatedAtDesc(channelId);
    }

    public Message getWithAuthor(UUID messageId) {
        return messageRepository.findWithAuthor(messageId)
                .orElseThrow(()-> new MessageNotFoundException(messageId));
    }

}
