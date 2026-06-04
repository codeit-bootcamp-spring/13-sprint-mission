package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    @Override
    public Channel create(String name, String description, User creator, ChannelType type) {
        Channel channel = new Channel(name, description, creator, type);
        channelRepository.save(channel);
        return channel;
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
