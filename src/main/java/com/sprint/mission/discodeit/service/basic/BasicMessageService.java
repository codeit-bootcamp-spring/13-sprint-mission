package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;

public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    private final ChannelRepository channelRepository;
    private final UserRepository userRepository;

    public BasicMessageService(UserRepository userRepository,
                               ChannelRepository channelRepository,
                               MessageRepository messageRepository){
        this.messageRepository = messageRepository;
        this.channelRepository = channelRepository;
        this.userRepository = userRepository;
    }



    @Override
    public void create(Message message) {
       User foundUser = userRepository.findById(message.getAuthorId());
        if(foundUser == null){
            throw new IllegalArgumentException("유저를 찾을 수 앖습니다.");
        }

        Channel foundChannel = channelRepository.findById(message.getChannelId());
        if(foundChannel == null){
            throw new IllegalArgumentException("채널을 찾을 수 없습니다.");
        }

        messageRepository.save(message);

    }

    @Override
    public Message read(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> readAll() {
        return messageRepository.findAll();
    }

    @Override
    public void update(UUID id, String message) {
    Message foundMessage = messageRepository.findById(id);
    if( foundMessage != null){
        foundMessage.updateMessage(message);
    }

    }

    @Override
    public void delete(UUID id) {
        messageRepository.delete(id);
    }
}
