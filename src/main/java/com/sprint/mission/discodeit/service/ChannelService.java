package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;


import java.util.Collection;
import java.util.UUID;

public interface ChannelService {
    void createChannel(String name, String description, ChannelType type);
    Channel readChannel(UUID id);
    Collection<Channel> readChannelAll();
    void updateChannel(UUID id, String name,String description,ChannelType type);
    void deleteChannel(UUID id);
}
