package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JCFMessageRepository implements MessageRepository {

    //필드
    private final List<Message> messages = new ArrayList<>();

    //ctor
    public JCFMessageRepository() {}

    //interface
    @Override
    public void save() {}

    @Override
    public void createMessage(Message message) {
        messages.add(message);
    }

    @Override
    public Optional<Message> findMessage(Message message) {
        if(messages.contains(message)) {
            return Optional.of(message);
        }
        return Optional.empty();
    }

    @Override
    public List<Message> findAll() {
        return messages;
    }

    @Override
    public void deleteMessage(Message message) {
        messages.remove(message);
    }
}
