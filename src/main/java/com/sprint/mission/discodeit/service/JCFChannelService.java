package com.sprint.mission.discodeit.service;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;


import java.util.*;

public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> data;

    public JCFChannelService() {
        this.data = new HashMap<>();
    }

    @Override //PUBLIC
    public Channel createChannel(String name) {
        Channel channel = new Channel(name);
        this.data.put(channel.getId(),channel);
        return channel;
    }
    @Override //PRIVATE
    public Channel createChannel(String name, ChannelType channelType) {
        Channel channel = new Channel(name, channelType);
        this.data.put(channel.getId(),channel);
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        return this.data.get(id);
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(this.data.values());
    }

    @Override //PUBLIC
    public Channel updateChannel(UUID id, String name) {
        Channel updateChannel = this.data.get(id);

        updateChannel.updateName(name);
        return updateChannel;
    }

    @Override //PRIVATE
    public Channel updateChannel(UUID id, String name, ChannelType channelType) {
        Channel updateChannel = this.data.get(id);

        updateChannel.updateName(name);
        updateChannel.updateIsChannelType(channelType);
        return updateChannel;
    }

    @Override
    public void deleteChannel(UUID id) {
        this.data.remove(id);
    }
}
