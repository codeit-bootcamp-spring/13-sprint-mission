package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {

    void createMessage(Message message);
    Optional<Message> findMessage(UUID messageId);
    List<Message> findAllMessagesByChannelId(UUID channelId);
    List<Message> findAll();
    void save();
    void deleteMessagesByChannelId(UUID channelId);
}
