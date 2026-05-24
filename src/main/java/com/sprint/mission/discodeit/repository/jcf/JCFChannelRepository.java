package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JCFChannelRepository implements ChannelRepository {

    //필드
    private final List<Channel> channels = new ArrayList<>();

    //ctor
    public JCFChannelRepository() {}

    //interface
    @Override
    public void save() {}

    @Override
    public boolean existsChannel(String name) {
        for (Channel channel : channels) {
            if (channel.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void createChannel(Channel channel) {
        channels.add(channel);
    }

    @Override
    public Optional<Channel> findChannel(Channel channel) {
        if (channels.contains(channel)){
            return Optional.of(channel);
        }
        return Optional.empty();
    }

    @Override
    public List<Channel> findAll() {
        return channels;
    }

    @Override
    public void deleteChannel(Channel channel) {
        channels.remove(channel);
    }
}
