package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.*;

public class JCFMessageService implements MessageService {

    Map<UUID, Message> messageMap = new LinkedHashMap<>();


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
    public Message update(UUID id, String content) {
        messageMap.get(id).updateContent(content);
        return messageMap.get(id);
    }

    @Override
    public void delete(Message message) {
        messageMap.remove(message.getId());
    }
}
