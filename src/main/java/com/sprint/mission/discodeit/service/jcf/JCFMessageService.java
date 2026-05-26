package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFMessageService implements MessageService {

    private final MessageRepository repository;

    public JCFMessageService(MessageRepository repository) {
        this.repository = repository;
    }


    @Override
    public void create(Message message) {
        repository.create(message);
    }

    @Override
    public Message read(UUID id) {
        return repository.read(id);
    }

    @Override
    public List<Message> readAll() {
        return repository.readAll();
    }

    @Override
    public void update(UUID id, Message message) {
        repository.update(id, message);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}