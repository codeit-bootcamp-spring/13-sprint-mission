package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

// Create Read Update Delete
public interface ChannelService {

    Channel create(Channel channel);
    Channel findById(UUID id);
    List<Channel> findAll();
    void update(UUID id, String name, String description, Channel.ChannelType type);
    void delete(UUID id);


}
