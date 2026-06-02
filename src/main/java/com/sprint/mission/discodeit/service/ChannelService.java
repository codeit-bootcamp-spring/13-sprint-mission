package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

// Create Read Update Delete
public interface ChannelService {

    Channel create(Channel.ChannelType type, String name, String description);
    Channel findById(UUID channelId);
    List<Channel> findAll();
    Channel update(UUID channelId, String newName, String newDescription);
    void delete(UUID channelId);


}
