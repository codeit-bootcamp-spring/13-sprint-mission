package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    //필드를 활용해 생성, 조회, 수정, 삭제하는 메소드를 구현하세요.
    void create(Message message);//생성

    Message read(UUID id);//조회

    List<Message> readAll();

    void update(UUID id, String message);

    void delete(UUID id);


}
