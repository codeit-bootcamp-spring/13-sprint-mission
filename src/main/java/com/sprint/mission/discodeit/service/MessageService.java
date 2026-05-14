package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Message;

import java.util.List;

public interface MessageService {

    Message create(Message message);
    Message findById(Long id);
    List<Message> findAll();
    void delete(Long id);
    void update(Message message);

}
