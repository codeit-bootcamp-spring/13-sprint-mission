package com.sprint.mission.discodeit.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.*;

public class JCFMessageService implements MessageService {

    private final Map<UUID, Message> data;

    public JCFMessageService() {
        this.data = new HashMap<>();
    }

    @Override
    public Message create(Message message) {
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
    public Message update(Message message) {
        if(!data.containsKey(message.getId())) {
            throw new IllegalArgumentException("존재하지 않는 메세지 입니다.");
        }
        data.put(message.getId(), message);
        return message;
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
