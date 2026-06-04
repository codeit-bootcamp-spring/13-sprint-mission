package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.Collection;
import java.util.UUID;

public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;
    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public void create(Channel channel) {
        channelRepository.save(channel);
    }

    @Override
    public Channel findById(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public Collection<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public void update(UUID id, String name, String description) {
        Channel channel = channelRepository.findById(id);
        if (channel != null) {
            channel.update(name, description);
            channelRepository.save(channel);
        }
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }
}
