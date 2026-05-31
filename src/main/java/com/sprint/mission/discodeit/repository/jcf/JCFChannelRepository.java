package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JCFChannelRepository implements ChannelRepository {
    private final Map<UUID, Channel> channels;
    public JCFChannelRepository()
    {
        this.channels = new ConcurrentHashMap<UUID, Channel>();
    }


    @Override
    public void save(Channel channel) {
        channels.put(channel.getId(), channel);
    }

    @Override
    public Channel findById(UUID id) {
        return channels.get(id);
    }

    @Override
    public Collection<Channel> findAll() {
        return channels.values();
    }

    @Override
    public void delete(UUID id) {
        channels.remove(id);
    }
}
