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
    public boolean existsChannelByName(String name) {
        return channels.stream()
                .anyMatch(chanel -> chanel.getName().equals(name));
    }

    @Override
    public void createChannel(Channel channel) {
        channels.add(channel);
    }

    @Override
    public Optional<Channel> findChannelByName(String name) {
        return channels.stream()
                .filter(channel -> channel.getName().equals(name))
                .findFirst();
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
