package com.sprint.mission.discodeit.service.file;


import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.UUID;

public class FileMessageService implements MessageService {

    private final MessageRepository repository;

    public FileMessageService(MessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public Message create(UUID authorId, UUID channelId, String content) {
        return repository.save(new Message(authorId, channelId, content));
    }

    @Override
    public Message findById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<Message> findAll() {
        return repository.findAll();
    }

    @Override
    public Message update(UUID id, String content) {
        Message message = repository.findById(id);

        if (message == null) {
            throw new IllegalArgumentException("Message not found");
        }
        message.updateContent(content);
        repository.save(message);

        return message;
    }

    @Override
    public void delete(UUID id) {
        repository.delete(id);

    }
}
