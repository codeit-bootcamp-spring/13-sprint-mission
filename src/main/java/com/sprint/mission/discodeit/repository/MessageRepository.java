package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


public interface MessageRepository {
    void save(Message message);
    List<Message> find(Predicate<Message> fn);
    void update(UUID id, String data);
    void delete(UUID id);
}

