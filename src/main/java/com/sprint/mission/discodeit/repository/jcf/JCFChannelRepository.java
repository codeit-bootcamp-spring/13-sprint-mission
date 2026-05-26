package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;

import java.util.*;

public class JCFChannelRepository implements ChannelRepository {

    private final Map<UUID, Channel> data;

    public JCFChannelRepository() {
        this.data = new HashMap<>();
    }


    @Override
    public void create(Channel channel) {
        data.put(channel.getId(), channel);
    }

    @Override
    public boolean exists(UUID id) {
        return data.containsKey(id);
    }

    @Override
    public Channel read (UUID id){
        return data.get(id);
    }

    @Override
    public List<Channel> readAll () {
        return new ArrayList<>(data.values());
    }

    @Override
    public void update (UUID id, Channel channel){
        data.put(id, channel);
    }

    @Override
    public void delete (UUID id){
        data.remove(id);
    }
}

