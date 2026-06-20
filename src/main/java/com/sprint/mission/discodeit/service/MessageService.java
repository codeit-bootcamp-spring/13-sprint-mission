package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;
import java.util.List;
import java.util.Optional;

public interface MessageService {
    void save(Message message);
    Optional<Message> findById(String id);
    List<Message> findAll();
    void update(Message message);
    void delete(String id);
}