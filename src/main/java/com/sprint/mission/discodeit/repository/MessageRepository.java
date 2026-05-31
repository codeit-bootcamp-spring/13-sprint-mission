package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.Collection;
import java.util.UUID;

public interface MessageRepository {
    void save(Message message);
    Message findById(UUID id);
    Collection<Message> findAll();
    void delete(UUID id);

}
