package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.repository.jcf.JCFChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;


import java.io.IOException;
import java.util.*;

public class JCFChannelService implements ChannelService {

    //private final Map<UUID, Channel> data=new HashMap<>();

    private final ChannelRepository channelRepository=new JCFChannelRepository();

    // User: UUID id, Long createdAt, updatedAt, String username, email
    // Channel: UUID id, Long createdAt, updatedAt, ChannelType type, String name
    // Message: UUID id, Long createdAt, updatedAt, String content, UUID channelId


    @Override
    public Channel createChannel(ChannelType type, String name, Long createdAt) throws IOException {
        Channel channel=new Channel(type, name, createdAt);
        //data.put(newChannel.getId(), newChannel);
        //return newChannel;
        return channelRepository.saveChannel(channel);
    }

    @Override
    public Optional<Channel> readChannel(UUID id) throws IOException { // 단건, 다건
        //return Optional.ofNullable(data.get(id));
        return channelRepository.fineChannel(id);
    }

    @Override
    public List<Channel> readChannels() throws IOException {
        return channelRepository.findChannels();
        //return new ArrayList<>(data.values());
    }

    @Override
    public Channel editChannel(UUID id, ChannelType newType, String newName, Long updatedAt) throws IOException {
        Channel channel=channelRepository.fineChannel(id)
                .orElseThrow();
        channel.updateChannel(newType, newName, updatedAt);
        return channel;

    }

    @Override
    public void deleteChannel(UUID id) throws IOException {
        channelRepository.deleteChannel(id);
        //data.remove(id);
    }
}
