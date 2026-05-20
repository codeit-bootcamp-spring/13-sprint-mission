package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    Channel create(Channel channel);
    Channel read(UUID id);
    List<Channel> readAllChannels();
    Channel update(UUID id,
                   String channelName,
                   String description,
                   ChannelType channelType);
    void delete(UUID id);

}
