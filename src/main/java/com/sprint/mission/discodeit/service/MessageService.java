package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    void createMessage(Message message);

    Message findMessage(UUID id);

    List<Message> findAllMessages();

    void updateMessage(UUID id, String content);

    void deleteMessage(UUID id);
    
}
