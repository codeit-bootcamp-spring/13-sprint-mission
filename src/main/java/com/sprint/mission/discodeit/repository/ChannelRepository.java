package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface ChannelRepository {
    void save(Channel channel);
    List<Channel> find(Predicate<Channel> fn);
    void update(UUID cnl, String name, String description, ChannelType type);
    void delete(UUID id);
}
