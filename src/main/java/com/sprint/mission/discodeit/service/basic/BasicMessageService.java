package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;

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
    public Message create(UUID userId, UUID channelId, String content) {
        if(userId==null){
            throw new IllegalArgumentException("존재하지 않는 계정입니다");
        }
        if(channelId==null){
            throw new IllegalArgumentException("존재하지 않는 채널입니다");
        }
        Message message = new Message(userId, channelId, content);
        return messageRepository.create(message);
    }

    @Override
    public Message read(UUID id) {
        Message message = messageRepository.read(id);
        if(message==null){
            throw new IllegalArgumentException("존재하지 않는 메시지입니다");
        }
        return message;
    }

    @Override
    public List<Message> readAll() {
        return messageRepository.readAll();
    }

    @Override
    public void update(UUID id, String content) {
        Message message = messageRepository.read(id);
        if(message==null){
            throw new IllegalArgumentException("존재하지 않는 메시지입니다.");
        }
        message.update(content);
        messageRepository.update(message);
    }

    @Override
    public void delete(UUID id) {
        messageRepository.delete(id);
    }
}
