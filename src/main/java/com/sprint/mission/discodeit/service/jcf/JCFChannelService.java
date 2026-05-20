package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.service.ChannelService;

import java.util.*;

public class JCFChannelService implements ChannelService {

    Map<UUID, Channel> channelMap = new LinkedHashMap<>();

    @Override
    public Channel create(Channel channel) {
        channelMap.put(channel.getId(), channel);
        return channel;
    }

    @Override
    public Channel read(UUID id) {
        return channelMap.get(id);
    }

    @Override
    public List<Channel> readAllChannels() {
        return new ArrayList<>(channelMap.values());
    }

    @Override
    public Channel update(UUID id,
                          String channelName,
                          String description,
                          ChannelType channelType) {

        Channel channel = channelMap.get(id);

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }

        channel.updateChannelName(channelName);
        channel.updateDescription(description);
        channel.updateChannelType(channelType);

        return channel;
    }

    @Override
    public void delete(UUID id) {
        channelMap.remove(id);
    }

    public Channel updateChannelName(UUID id, String channelName) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.updateChannelName(channelName);
        return channel;
    }

    public Channel updateDescription(UUID id, String description) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.updateDescription(description);
        return channel;
    }

    public Channel updateChannelType(UUID id, ChannelType channelType) {
        Channel channel = channelMap.get(id);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.updateChannelType(channelType);
        return channel;
    }


    // 채널에 유저 추가
    public Channel addMember(UUID channelId, User user) {
        Channel channel = channelMap.get(channelId);
        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널입니다.");
        }
        channel.addMember(user);
        return channel;
    }


}

