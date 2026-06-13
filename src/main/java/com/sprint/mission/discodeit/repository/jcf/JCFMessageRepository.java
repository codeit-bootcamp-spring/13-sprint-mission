package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> data = new HashMap<>();

    @Override
    public Message create(Message message) {
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Message read(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Message> readAll() {
        return data.values().stream().toList();
    }

    @Override
    public void update(Message message) {
        if (data.containsKey(message.getId())) {
            data.put(message.getId(), message);
        }
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
