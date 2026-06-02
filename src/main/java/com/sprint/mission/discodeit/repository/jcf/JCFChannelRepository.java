package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelRepository implements ChannelRepository {
    private final List<Channel> channels = new ArrayList<>();
    public Channel save(Channel channel) {
        channels.add(channel);
        return channel;
    }
    public Channel findByld(UUID id) {
        for (Channel c : channels) {
            if (c.getId().equals(id))
                return c;
        }
        return null;
    }
}
