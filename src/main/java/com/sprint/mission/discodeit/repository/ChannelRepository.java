package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.Collection;
import java.util.UUID;

public interface ChannelRepository {
    void save(Channel channel);
    Channel findById(UUID id);
    Collection<Channel> findAll();
    void delete(UUID id);

}
