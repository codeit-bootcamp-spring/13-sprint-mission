package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> data;

    public JCFChannelService() {
        this.data = new HashMap<>();
    }

    @Override
    public Channel create(Channel.ChannelType type, String name, String description) {
        Channel channel = new Channel(name, description, type);
        data.put(channel.getId(), channel);
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Channel update(UUID channelId, String newName, String newDescription) {
        Channel channel = data.get(channelId);

        if (channel == null) {
            throw new IllegalArgumentException("Channel not found");
        }
        channel.updateName(newName);
        channel.updateDescription(newDescription);
        return channel;
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }


}
