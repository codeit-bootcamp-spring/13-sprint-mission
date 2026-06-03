package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;
import java.util.UUID;
import java.util.List;

public abstract class MessageService {
    public abstract void create(Message message);
    public abstract Message read(UUID id);
    public abstract List<Message> readAll();
    public abstract void update(Message message);
    public abstract void delete(UUID id);
}
