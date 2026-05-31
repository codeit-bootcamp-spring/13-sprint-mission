package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.util.List;

public class FileMessageRepository implements MessageRepository {

    private final MessageRepository messageRepository;

    public FileMessageRepository() {
        this.messageRepository = new FileMessageRepository();
    }

    @Override
    public Message create(Message message) {
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
    public void update(Message requestMessage) {
        messageRepository.update(requestMessage);
    }

    @Override
    public void delete(String content) {
        messageRepository.delete(content);
    }

}
/*
레포지토리 설계 및 구현
[ ] 다음의 조건을 만족하는 레포지토리 인터페이스의 구현체를 작성하세요.
[ ] 기존에 구현한 File*Service 구현체의 "저장 로직"과 관련된 코드를 참고하여 구현하세요.
 */