package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.ChannelService;
import com.sprint.mission.discodeit.service.MessageService;
import com.sprint.mission.discodeit.service.UserService;

import java.util.*;

public class JCFMessageService implements MessageService {

    private final UserService userService;
    private final ChannelService channelService;
    private final Map<UUID, Message> data;

    public JCFMessageService(UserService userService, ChannelService channelService) {
        this.userService = userService;
        this.channelService = channelService;
        this.data = new HashMap<>();
    }

    @Override
    public Message create(UUID authorId, UUID channelId, String content) {

        if (userService.findById(authorId) == null) {
            throw new IllegalArgumentException("User not found");
        }
        if (channelService.findById(channelId) == null) {
            throw new IllegalArgumentException("Channel not found");
        }

        Message message = new Message(authorId, channelId, content);

        data.put(message.getId(), message);

        return message;
    }

    @Override
    public Message findById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Message> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Message update(UUID id, String content) {
        Message message = data.get(id);

        if (message == null) {
            throw new IllegalArgumentException("Message not found");
        }
        message.updateContent(content);
        return message;
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }


}