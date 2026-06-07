package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message create(String content, UUID channelId, UUID authorId);

    Message find(UUID id);

    List<Message> findAll();

    void update(UUID id, String content);

    void delete(UUID id);
    
}
