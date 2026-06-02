package com.sprint.mission.discodeit.repository.jcf;


import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.*;

public class JCFChannelRepository implements ChannelRepository {

    private final Map<UUID, Channel> data=new HashMap<>();

    @Override
    public Channel createOne(Channel channel) {
        data.put(channel.getId(), channel);
        return channel;
    }

    @Override
    public Optional<Channel> readOne(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public List<Channel> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void deleteOne(UUID id) {
        data.remove(id);
    }
}
