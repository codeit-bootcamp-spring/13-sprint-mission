package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.*;

public class JCFMessageRepository implements MessageRepository {

    private final Map<UUID, Message> data=new HashMap<>();

    @Override
    public Message saveMessage(Message message) {
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public Optional<Message> findMessage(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Message> findMessages() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteMessage(UUID id) {
        data.remove(id);
    }


}
