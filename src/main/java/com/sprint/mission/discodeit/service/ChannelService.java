package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    Channel create(String name, Channel.ChannelType channelType, String description);

    Channel find(UUID id);

    List<Channel> findAll();

    void update(UUID id, String name, Channel.ChannelType type, String description);

    void delete(UUID id);

}
