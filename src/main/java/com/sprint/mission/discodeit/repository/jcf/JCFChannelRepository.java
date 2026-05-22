package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Predicate;

public class JCFChannelRepository implements ChannelRepository {
    private final HashMap<UUID, Channel> data;

    public JCFChannelRepository() {
        data = new HashMap<>();
    }

    @Override
    public void create(String name, String description, ChannelType type) {
        Channel channel = new Channel(name, description, type);
        for (int i = 0; i < 3; i++){
            if (this.data.containsKey(channel
                    .getId())) channel
                    = new Channel(name, description, type);
        }
        data.put(channel.getId(),channel);
    }

    @Override
    public ArrayList<Channel> select (Predicate<Channel> fn) {
        return new ArrayList<>(data.values().stream()
                .filter(fn)
                .toList());
    }

    @Override
    public void update(UUID id, String name,String description,ChannelType type) {
        Channel cnl = data.get(id);
        cnl.setName(name);
        cnl.setDescription(description);
        cnl.setType(type);
        cnl.setUpdatedAt(System.currentTimeMillis());
    }

    @Override
    public void delete(UUID channel) {
        data.remove(channel);
    }
}
