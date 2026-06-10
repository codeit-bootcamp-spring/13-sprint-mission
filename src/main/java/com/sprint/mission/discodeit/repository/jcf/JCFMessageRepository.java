package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.dto.response.*;
import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFMessageRepository implements MessageRepository {

    private final Map<UUID, Message> data;

    public JCFMessageRepository(UserService userService,
                             ChannelService channelService) {

        this.data = new HashMap<>();
    }

    @Override
    public void create(Message message) {
        data.put(message.getId(), message);
    }

    @Override
    public boolean exists(UUID id) {
        return data.containsKey(id);
    }

    @Override
    public Message find(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Message> findAllByChannelId(UUID channelId) {
        return data.values().stream()
                .filter(message -> message.getChannelId().equals(channelId))
                .toList();
    }

    @Override
    public void update(UUID id, Message message) {
        data.put(id, message);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
