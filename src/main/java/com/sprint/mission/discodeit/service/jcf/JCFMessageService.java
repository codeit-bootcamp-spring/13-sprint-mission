package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JCFMessageService implements MessageService {

    private final Map<Long, Message> messages;

    public JCFMessageService() {
        this.messages = new HashMap<>();
    }

    @Override
    public Message create(Message message) {
        return null;
    }

    @Override
    public Message findById(Long id) {
        return null;
    }

    @Override
    public List<Message> findAll() {
        return List.of();
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public void update(Message message) {

    }
}
