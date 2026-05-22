package com.sprint.mission.discodeit.service.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface MessageRepository {

    public void save(Message message);

    public Message findById(UUID id);

    public List<Message> findAll();

    public void deleteById(UUID id);

    public void update(Message message);
}
