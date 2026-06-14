package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {
    Message create (Message message);
    Message findByContent(String content);
    List<Message> findAll();
    void update(Message message);
    void delete(String content);
}