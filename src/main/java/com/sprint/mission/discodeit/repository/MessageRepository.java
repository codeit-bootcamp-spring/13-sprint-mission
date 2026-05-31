package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
    void create(Message message);

    Message read(UUID id);

    List<Message> readAll();

    void update(Message message);

    void delete(UUID id);
}
