package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {

    void createMessage(Message message);
    Optional<Message> findMessageById(UUID id);
    List<Message> findAllMessagesByChannelId(UUID channelId);
    List<Message> findAllMessagesByUserId(UUID userId);
    void save();
    void deleteMessagesByChannelId(UUID channelId);
    void deleteMessageById(UUID id);
}
