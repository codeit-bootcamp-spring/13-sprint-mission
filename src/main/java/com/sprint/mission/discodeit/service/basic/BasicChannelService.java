package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

//    public BasicChannelService(ChannelRepository channelRepository) {
//        this.channelRepository = channelRepository;
//    }

    @Override
    public Channel create(String name, Channel.ChannelType channelType, String description) {
        Channel channel = new Channel(name, channelType, description);
        return channelRepository.save(channel);
    }

    @Override
    public Channel find(UUID id) {
        return channelRepository.findById(id);
    }

    @Override
    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    @Override
    public void update(UUID id, String name, Channel.ChannelType type, String description) {
        Channel channel = channelRepository.findById(id);
        channel.update(name, type, description);

        channelRepository.save(channel);
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }
}
