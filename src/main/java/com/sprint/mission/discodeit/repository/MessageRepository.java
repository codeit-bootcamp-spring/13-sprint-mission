package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageRepository {
    void save(Message message);
    Message findById(UUID messageId);
    List<Message> findAllByChannelId(UUID channelId);
    List<Message> findAllByUserId(UUID userId);
    void deleteById(UUID id);

}
