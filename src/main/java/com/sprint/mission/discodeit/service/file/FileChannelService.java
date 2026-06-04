package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {
    private final ChannelRepository channelRepository;

    public FileChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    @Override
    public Channel create(ChannelType type, String channelName, String description) {
        Channel channel = new Channel(type, channelName, description);
        channelRepository.create(channel);
        System.out.println(channelName + " 채널이 생성되었습니다!");
        return channel;
    }

    @Override
    public Channel read(UUID id) {
        Channel channel = channelRepository.read(id);
        if (channel == null) {
            System.out.println("채널이 존재하지 않습니다.");
        }
        return channel;
    }

    @Override
    public List<Channel> readAll() {
        return channelRepository.readAll();
    }

    @Override
    public void update(UUID id, ChannelType type, String channelName, String description) {
        Channel channel = channelRepository.read(id);
        if (channel != null) {
            channel.update(type, channelName, description);
            channelRepository.update(channel);
        } else {
            System.out.println("수정할 채널이 존재하지 않습니다.");
        }
    }

    @Override
    public void delete(UUID id) {
        channelRepository.delete(id);
    }
}