package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

public class JCFChannelService implements ChannelService {

    Map<UUID, Channel> channelMap = new HashMap<UUID, Channel>();

    @Override
    public Channel create(Channel channel) {
        channelMap.put(channel.getId(), channel);
        return channel;
    }

    @Override
    public Channel read(UUID id) {
        return channelMap.get(id);
    }

    @Override
    public List<Channel> readAllChannels() {
        return new ArrayList<>(channelMap.values());
    }

    @Override
    public Channel update(UUID id, Channel channel) {
        channelMap.get(id).updateChannelName(channel.getChannelName());
        return channelMap.get(id);
    }

    @Override
    public void delete(UUID id) {
        channelMap.remove(id);
    }
}
