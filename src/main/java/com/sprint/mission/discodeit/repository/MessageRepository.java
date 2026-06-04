package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Message;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository {
    Message saveMessage(Message message) throws IOException;

    Optional<Message> findMessage(UUID id) throws IOException;

    List<Message> findMessages() throws IOException;
    // Repository 계층에서는 read, create < find, save 키워드 선호

    void deleteMessage(UUID id) throws IOException;
}
