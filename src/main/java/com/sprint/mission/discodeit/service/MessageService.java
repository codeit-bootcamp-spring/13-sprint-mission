package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message create(String content, UUID channelId, UUID authorId);

    Message read(UUID messageId);

    List<Message> readAll();

    Message update(UUID id, String content);

    void delete(UUID messageId);
}
