package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface MessageRepository {

    void create(Message message);

    Optional<Message> findById(UUID id);

    List<Message> findAllByChannelId(UUID channelId);

    void update(UUID id, Message message);

    void delete(UUID id);

    boolean exists(UUID id);

}
