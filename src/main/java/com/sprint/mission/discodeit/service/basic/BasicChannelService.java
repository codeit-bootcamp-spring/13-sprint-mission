package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.UUID;

public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    public BasicChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public void createChannel(Channel channel) {
        channelRepository.save(channel);
    }

    @Override
    public Channel findChannel(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> findAllChannels() {
        return channelRepository.findAll();
    }

    @Override
    public void updateChannel(UUID id, String name, Channel.ChannelType type, String description) {
        Channel channel = channelRepository.findById(id);
        channel.update(name, type, description);

        channelRepository.save(channel);
    }

    @Override
    public void deleteChannel(UUID id) {
        channelRepository.delete(id);
    }
}
