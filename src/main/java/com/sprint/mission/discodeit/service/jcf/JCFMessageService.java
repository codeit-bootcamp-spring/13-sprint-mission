package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;

public class JCFMessageService implements MessageService {

    private final MessageRepository repository;

    public JCFMessageService(MessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public Message create(Message message) {
        return repository.save(message);
    }

    @Override
    public Message find(UUID id) {
        return repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Message not found"));
    }

    @Override
    public List<Message> findAll() {
        return repository.findAll();
    }

    @Override
    public Message update(UUID id, String content) {

        Message message = find(id);

        message.update(content);

        return repository.save(message);
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);
    }
}