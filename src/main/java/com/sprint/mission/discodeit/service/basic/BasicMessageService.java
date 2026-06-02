package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;

    public BasicMessageService(MessageRepository messageRepository, UserRepository userRepository, ChannelRepository channelRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.channelRepository = channelRepository;
    }

    @Override
    public Message create(UUID authorId, UUID channelId, String content) {
        if (userRepository.findById(authorId) == null) {
            throw new IllegalArgumentException("유저를 찾을 수 없습니다.");
        }
        if (channelRepository.findById(channelId) == null) {
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }

        return messageRepository.save(new Message(authorId, channelId, content));

    }

    @Override
    public Message findById(UUID messageId) {
        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new NoSuchElementException("메세지를 찾을 수 없습니다.");
        }
        return message;
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public Message update(UUID messageId, String newContent) {

        Message message = messageRepository.findById(messageId);

        if (message == null) {
            throw new NoSuchElementException("메세지를 찾을 수 없습니다.");
        }

        message.updateContent(newContent);
        return messageRepository.save(message);

    }

    @Override
    public void delete(UUID messageId) {

        Message message = messageRepository.findById(messageId);
        if (message == null) {
            throw new NoSuchElementException("메세지를 찾을 수 없습니다.");
        }

        messageRepository.delete(messageId);
    }
}
