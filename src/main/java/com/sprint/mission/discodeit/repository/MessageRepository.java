package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;


public interface MessageRepository {
    void save(Message message);
    List<Message> find(Predicate<Message> fn);
    List<Message> findByChannelID(UUID channelID);
    void delete(UUID id);
}

