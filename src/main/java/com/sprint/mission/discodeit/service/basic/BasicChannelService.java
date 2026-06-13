package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
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
    public Channel create(ChannelType type, String channelName, String description) {
        Channel channel = new Channel(type, channelName, description);
        return channelRepository.create(channel);
    }

    @Override
    public Channel read(UUID id) {
        Channel channel = channelRepository.read(id);
        if(channel==null){
            throw new IllegalArgumentException("존재하지 않는 채널입니다");
        }
        return channel;
    }

    @Override
    public List<Channel> readAll() {
        return channelRepository.readAll();
    }

    @Override
    public void update(UUID id, ChannelType type, String channelName, String description) {
        Channel channel = channelRepository.read(id);
        if(channel==null){
            throw new IllegalArgumentException("존재하지 않는 채널입니다");
        }
        channel.update(type, channelName, description);
        channelRepository.update(channel);
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }
}
