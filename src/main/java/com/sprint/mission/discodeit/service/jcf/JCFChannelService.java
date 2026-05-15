package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.util.*;

public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> data;

    public JCFChannelService() {
        this.data = new HashMap<>();
    }


    @Override
    public void create(Channel channel) {
        data.put(channel.getId(), channel);
    }

    @Override
    public Channel read(UUID id) {
        return data.get(id);
    }

    @Override
    public List<Channel> readAll() {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update(UUID id, Channel channel) {
        data.put(id, channel);
    }

    @Override
    public void delete(UUID id) {
        data.remove(id);
    }
}
