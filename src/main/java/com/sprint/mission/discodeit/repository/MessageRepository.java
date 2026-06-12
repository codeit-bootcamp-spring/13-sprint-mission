package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    void save(Message message);
    Optional<Message> findById(UUID messageId);
    List<Message> findAllByChannelId(UUID channelId);
    void deleteById(UUID id);

}
