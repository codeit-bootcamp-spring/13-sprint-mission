package com.sprint.mission.discodeit.service.jcf;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;


import java.util.*;

public class JCFChannelService implements ChannelService {

    private final Map<UUID, Channel> data;

    public JCFChannelService() {
        this.data = new HashMap<>();
    }

    @Override //PUBLIC
    public Channel createChannel(String name, String description) {
        boolean duplicateCheck = data.values().stream()
                .anyMatch(channel -> channel.getName().equals(name));
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("채널이름을 입력해 주세요!");
        }
        if (description == null || description.isBlank()){
            throw new IllegalArgumentException("채널 설명을 작성해 주세요!");
        }
        if (duplicateCheck){
            throw new IllegalArgumentException("이미 동일한 채널명이 존재합니다.");
        }
        Channel channel = new Channel(name, description);
        data.put(channel.getId(),channel);
        return channel;

    }
    @Override //PRIVATE
    public Channel createChannel(String name, String description,ChannelType channelType) {
        Channel channel = new Channel(name, description, channelType);
        data.put(channel.getId(),channel);
        return channel;
    }

    @Override
    public Channel findByChannel(UUID channelId) {
        Channel channel = data.get(channelId);
        if (channel == null) {
            throw new NoSuchElementException("존재하지 않는 채널입니다.");
        }
        return channel;
    }

    @Override
    public List<Channel> findAllChannel() {
        return new ArrayList<>(data.values());
    }

    @Override
    public Channel updateChannel(UUID channelId, String name, String description, ChannelType channelType) {
        Channel updateChannel = data.get(channelId);
        if (updateChannel == null){
            throw new NoSuchElementException("존재하지 않는 채널입니다.");
        }
        if (name != null && !name.isBlank()){
            updateChannel.updateChannel(name);
        }
        if (description != null && !description.isBlank()){
            updateChannel.updateChannelDescription(description);
        }
        if (channelType != null){
            updateChannel.updateIsChannelType(channelType);
        }
        return updateChannel;
    }

    @Override
    public void deleteChannel(UUID channelId) {
        Channel deleteChannel = data.get(channelId);
        if (deleteChannel == null){
            throw new NoSuchElementException("존재하지 않는 채널입니다.");
        }
        data.remove(channelId);
    }
}
