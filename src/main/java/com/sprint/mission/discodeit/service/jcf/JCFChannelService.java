package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

public class JCFChannelService implements ChannelService {
    private final Map<UUID, Channel> data;

    public JCFChannelService() {
        this.data = new HashMap<>();
    }

    @Override
    public Channel create(ChannelType type, String channelName, String description) {
        Channel channel = new Channel(type, channelName, description);
        data.put(channel.getId(), channel);
        System.out.println(channelName+" 채널이 생성되었습니다!");
        return channel;
    }

    @Override
    public Channel read(UUID id) {
        if (!data.containsKey(id)) {
            System.out.println("채널이 존재하지 않습니다.");
        }
        return data.get(id);
    }

    @Override
    public List<Channel> readAll() {
        return data.values().stream()
                .toList();
    }

    @Override
    public void update(UUID id, ChannelType type, String channelName, String description) {
        if(data.containsKey(id)){
            Channel channel = data.get(id);
            channel.update(type, channelName, description);
        }
    }

    @Override
    public void delete(UUID id) {
        if(!data.containsKey(id)){
            System.out.println("채널이 존재하지 않습니다.");
        }
        data.remove(id);
    }
}
