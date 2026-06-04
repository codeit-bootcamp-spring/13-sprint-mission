package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    Channel create(ChannelType type, String channelName, String description);

    Channel read(UUID id);

    List<Channel> readAll();

    void update(UUID id, ChannelType type, String channelName, String description);

    void delete(UUID id);
}
