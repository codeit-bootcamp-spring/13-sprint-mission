package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFMessageRepository implements MessageRepository {
    private final List<Message> messages = new ArrayList<>();

    public Message save(Message message) {
        messages.add(message);
        return message;
    }
    public Message findByld(UUID id) {
        for (Message m : messages) {
            if (m.getId().equals(id))
                return m;
        }
        return null;
    }
}
