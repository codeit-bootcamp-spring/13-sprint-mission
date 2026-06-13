package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

//메시지에 대한 비즈니스 로직을 담당하는 service 계층
public class BasicMessageService implements MessageService {
    private final MessageRepository messageRepository;

    public BasicMessageService(MessageRepository messageRepository) { this.messageRepository = messageRepository; }

    @Override //메시지 생성
    public Message create(String content, UUID channelId, UUID authorId) {
        Message message = new Message(content, channelId, authorId);
        return messageRepository.save(message);
    }

    @Override //메시지 단건조회
    public Message find(UUID messageId) {
        return messageRepository.findById(messageId)
                .orElseThrow(()-> new NoSuchElementException("Message with id " + messageId + " not found"));
    }

    @Override //전체 메시지 조회
    public List<Message> findAll() {return messageRepository.findAll();}

    @Override //메시지 수정
    public Message update(UUID messageId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(()-> new NoSuchElementException("Message with id " + messageId + " not found"));
        message.update(newContent);
        return messageRepository.save(message);
    }

    @Override //메시지 삭제
    public void delete(UUID messageId) {
        if (!messageRepository.existsById(messageId)) {
            throw new NoSuchElementException("Message with id " + messageId + " not found");
        }
        messageRepository.deleteById(messageId);
    }
}
