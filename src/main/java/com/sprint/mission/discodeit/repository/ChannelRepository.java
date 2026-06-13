package com.sprint.mission.discodeit.repository;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelRepository {
    Channel create(Channel channel);

    Channel read(UUID id);

    List<Channel> readAll();

    void update(Channel channel);

    void delete(UUID id);
}
