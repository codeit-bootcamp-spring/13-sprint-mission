package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Predicate;

public interface ChannelRepository {
    void create(String name, String description, ChannelType type);
    ArrayList<Channel> select(Predicate<Channel> fn);
    void update(UUID cnl, String name, String description, ChannelType type);
    void delete(UUID id);
}
