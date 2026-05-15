package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.*;

public class JCFMessageService implements MessageService {

    Map<UUID, Message> messageMap = new HashMap<UUID, Message>();


    @Override
    public Message create(Message message) {
        messageMap.put(message.getId(), message);
        return message;
    }

    @Override
    public Message read(UUID id) {
        return messageMap.get(id);
    }

    @Override
    public List<Message> readAll() {
        return new ArrayList<>(messageMap.values());
    }

    @Override
    public Message update(UUID id, Message message) {
        messageMap.get(id).updateContent(message.getContent());
        return messageMap.get(id);
    }

    @Override
    public void delete(Message message) {
        messageMap.remove(message.getId());
    }
}
