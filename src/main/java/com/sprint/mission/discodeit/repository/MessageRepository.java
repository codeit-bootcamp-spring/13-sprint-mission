package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.User;

import java.util.ArrayList;
import java.util.function.Predicate;


public interface MessageRepository {
    void create(User user, Channel channel, String data);
    ArrayList<Message> select(Predicate<Message> fn);
    void update(Message msg, String data);
    void delete(Message message);
}

