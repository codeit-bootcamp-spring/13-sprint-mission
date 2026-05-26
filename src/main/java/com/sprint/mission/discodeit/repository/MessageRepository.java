package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface MessageRepository {

    void create(Message message);

    Message read(UUID id);

    List<Message> readAll();

    void update(UUID id, Message message);

    void delete(UUID id);

}
