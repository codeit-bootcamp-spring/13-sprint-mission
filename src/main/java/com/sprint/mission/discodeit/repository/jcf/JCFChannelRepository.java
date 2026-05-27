package com.sprint.mission.discodeit.repository.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class JCFChannelRepository implements ChannelRepository {
    private final List<Channel> data = new ArrayList<>();

    @Override
    public void save(Channel channel) {

        data.add(channel);
    }

    @Override
    public Channel findById(UUID id) {
        for(Channel channel : data){
            if(channel.getId().equals(id)){
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Channel> findAll() {
        return data;
    }

    @Override
    public void delete(UUID id) {
    data.remove(findById(id));

    }
}
