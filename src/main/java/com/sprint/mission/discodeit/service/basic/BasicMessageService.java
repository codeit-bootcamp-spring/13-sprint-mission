package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChannelRepository channelRepository;


    @Override
    public Message createContent(String content, UUID channelId, UUID authorId) {
        if (content == null || content.isBlank()) {
            throw new NoSuchElementException("메시지를 입력해 주세요.");
        }
        if (userRepository.findById(authorId) == null){
            throw new NoSuchElementException("존재하지 않는 유저 입니다.");
        }
        if(channelRepository.findById(channelId) == null){
            throw new NoSuchElementException("존재하지 않는 채널 입니다.");
        }
        Message message = new Message(content, channelId, authorId);
        messageRepository.save(message);
        return message;


    }

    @Override
    public Message findByMessage(UUID messageId) {
        Message byId = messageRepository.findById(messageId);
        if (byId == null) {
            throw new NoSuchElementException("존재하지 않는 메시지 입니다.");
        }
        return byId;
    }

    @Override
    public List<Message> findAllByMessage(UUID channelId) {
        return messageRepository.findAllByChannelId(channelId);
    }

    @Override
    public Message updateContent(UUID messageId, String content) {
        Message message = messageRepository.findById(messageId);
        if(message == null) {
            throw new NoSuchElementException("존재 하지 않는 메시지 입니다.");
        }
        message.updateContent(content);
        messageRepository.save(message);
        return message;
    }

    @Override
    public void deleteMessage(UUID messageId) {
        Message byId = messageRepository.findById(messageId);
        if (byId == null) {
            throw new NoSuchElementException("존재 하지 않는 메시지 입니다.");
        }
        messageRepository.deleteById(messageId);
    }
}
