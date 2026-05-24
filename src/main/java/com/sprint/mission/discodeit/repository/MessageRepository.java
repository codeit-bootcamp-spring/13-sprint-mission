package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {

    void save();
    void createMessage(Message message);
    Optional<Message> findMessage(Message message);
    List<Message> findAll();
    void deleteMessage(Message message);
}
