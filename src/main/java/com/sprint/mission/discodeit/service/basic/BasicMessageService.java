package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;

public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;
    public BasicMessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void createMessage(Message message) {
        messageRepository.save(message);
    }

    @Override
    public Message findMessage(UUID id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> findAllMessages() {
        return  messageRepository.findAll();
    }

    @Override
    public void updateMessage(UUID id, String content) {
        Message message = messageRepository.findById(id);
        message.update(content);

        messageRepository.save(message);
    }

    @Override
    public void deleteMessage(UUID id) {
        messageRepository.delete(id);
    }

}
