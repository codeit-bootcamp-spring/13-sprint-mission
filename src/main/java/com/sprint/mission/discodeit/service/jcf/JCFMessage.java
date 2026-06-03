package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.*;

public class JCFMessage implements MessageService {

    private final Map<UUID, Message> data;

    public JCFMessage() {

        this.data = new HashMap<>();
    }

    @Override
    public void create(Message message) {

        data.put(message.getId(), message);
    }

    @Override
    public Message read(UUID id) {

        return data.get(id);
    }

    @Override
    public List<Message> readAll() {

        return new ArrayList<>(data.values());
    }

    @Override
    public void delete(UUID id) {

        data.remove(id);
    }
}
