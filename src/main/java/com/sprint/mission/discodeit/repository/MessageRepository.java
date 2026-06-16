package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Message save(Message message);
    Optional<Message> findById(UUID id);
    List<Message> findAll();
    void deleteById(UUID id); // Repository 계층에서는 read, create < findById, save 키워드 선호
    boolean existById(UUID id);
    List<Message> findAllByChannelId(UUID channelId);
}
