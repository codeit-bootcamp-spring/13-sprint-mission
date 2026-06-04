package com.sprint.mission.discodeit.repository.jcf;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.*;

public class JCFChannelRepository implements ChannelRepository {

    private final Map<UUID, Channel> data=new HashMap<>();

    @Override
    public Channel saveChannel(Channel channel) {
        data.put(channel.getId(), channel);
        return channel;
    }

    @Override
    public Optional<Channel> fineChannel(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Channel> findChannels() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteChannel(UUID id) {
        data.remove(id);
    }
}
