package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.file.FileMessageRepository;
import com.sprint.mission.discodeit.service.MessageService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class FileMessageService implements MessageService {

    private final MessageRepository messageRepository;

    public FileMessageService() {
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
기본 요구사항
File IO를 통한 데이터 영속화
[ ]  JCF 대신 FileIO와 객체 직렬화를 활용해 메소드를 구현하세요.
서비스 구현체 분석
[ ] JCF*Service 구현체와 File*Service 구현체를 비교하여 공통점과 차이점을 발견해보세요.
[ ] "비즈니스 로직"과 관련된 코드를 식별해보세요.
[ ] "저장 로직"과 관련된 코드를 식별해보세요.
 */