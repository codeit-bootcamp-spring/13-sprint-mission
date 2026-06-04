package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JCFMessageRepository implements MessageRepository {
    private final Map<UUID, Message> message;


    public JCFMessageRepository( ) {
        this.message = new ConcurrentHashMap<UUID, Message>();
    }


    @Override
    public void save(Message message) {
        this.message.put(message.getId(), message);
    }

    @Override
    public Message findById(UUID id) {
        return message.get(id);
    }

    @Override
    public Collection<Message> findAll() {
        return message.values();
    }

    @Override
    public void delete(UUID id) {
        message.remove(id);
    }
}
