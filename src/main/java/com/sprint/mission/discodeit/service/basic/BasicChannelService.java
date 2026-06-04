package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.List;
import java.util.UUID;

public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    public BasicChannelService (ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public void create(Channel channel) {
        channelRepository.save(channel);

    }

    @Override
    public Channel read(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> readAll() {
        return channelRepository.findAll();
    }

    @Override
    public void update(UUID id, String name, String description) {
        Channel foundChannel = channelRepository.findById(id);
        if (foundChannel != null){
            foundChannel.update(name,description);
        }

    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);

    }
}
