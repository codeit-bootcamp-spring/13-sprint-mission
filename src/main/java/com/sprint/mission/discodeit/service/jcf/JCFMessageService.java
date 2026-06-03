package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import java.util.List;
import java.util.Optional;

public class JCFMessageService implements MessageService {
    private final MessageRepository messageRepository;

    public JCFMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void save(Message message) {
        messageRepository.save(message);
    }

    @Override
    public Optional<Message> findById(String id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return messageRepository.findAll();
    }

    @Override
    public void update(Message message) {
        messageRepository.update(message);
    }

    @Override
    public void delete(String id) {
        messageRepository.delete(id);
    }
}