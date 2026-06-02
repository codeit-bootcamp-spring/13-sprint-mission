package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicMessageService implements MessageService {

    private final MessageRepository messageRepository;

    @Override
    public Message create (Message message) {
        return messageRepository.create(message);
    }

    @Override
    public Message findByContent(String content) {
        return messageRepository.findByContent(content);
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
    public void delete(String content) {
        messageRepository.delete(content);
    }
}
