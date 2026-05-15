package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.*;

import java.util.*;

public interface MessageService {

    void create(Message message);

    Message read(UUID id);

    List<Message> readAll();

    void update(UUID id, Message message);

    void delete(UUID id);


}
