package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public interface ChannelRepository {
    void save(Channel channel);
    List<Channel> find(Predicate<Channel> fn);
    void delete(UUID id);
}
