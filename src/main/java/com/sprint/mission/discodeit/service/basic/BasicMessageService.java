package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.Collection;
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
    public void create(Message message) {
        User author = userRepository.findById(message.getAuthor().getId());
        Channel channel = channelRepository.findById(message.getChannel().getId());

        if (author == null) {
            throw new IllegalArgumentException("존재하지 않는 유저의 메시지입니다.");
        }

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널의 메시지입니다.");
        }

        messageRepository.save(message);
    }



    @Override
    public Message findById(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public Collection<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public void update(UUID id, String content) {
        Message message = messageRepository.findById(id);
        if (message != null) {
            message.update(content);
            messageRepository.save(message);
        }
    }

    @Override
    public void delete(UUID id) {
        messageRepository.delete(id);
    }
}
