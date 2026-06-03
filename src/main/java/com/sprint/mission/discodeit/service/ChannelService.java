package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;

import java.util.List;
import java.util.UUID;

public interface ChannelService {

    void createChannel(Channel channel);

    Channel findChannel(UUID id);

    List<Channel> findAllChannels();

    void updateChannel(UUID id, String name, Channel.ChannelType type, String description);

    void deleteChannel(UUID id);

}
