package com.sprint.mission.discodeit.service.basic;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import com.sprint.mission.discodeit.service.ChannelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BasicChannelService implements ChannelService {

    private final ChannelRepository channelRepository;

    @Override
    public Channel createChannel(String name, String description) {
        boolean duplicateCheck = channelRepository.findAll().stream()
                .anyMatch(channel -> channel.getName().equals(name));
        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("채널명을 입력해주세요.");
        }
        if (description == null || description.isBlank()){
            throw new IllegalArgumentException("채널설명을 입력해주세요.");
        }
        if (duplicateCheck) {
            throw new IllegalArgumentException("이미 동일한 채널명이 존재합니다.");
        }
        Channel channel = new Channel(name, description);
        channelRepository.save(channel);
        return channel;
    }

    @Override
    public Channel createChannel(String name, String description, ChannelType channelType) {
        boolean duplicateCheck = channelRepository.findAll().stream()
                .anyMatch(channel -> channel.getName().equals(name));
        if (duplicateCheck) {
            throw new IllegalArgumentException("이미 동일한 채널명이 존재합니다.");
        }

        if (name == null || name.isBlank()){
            throw new IllegalArgumentException("채널명을 입력해주세요.");
        }
        if (description == null || description.isBlank()){
            throw new IllegalArgumentException("채널설명을 입력해주세요.");
        }
        Channel channel = new Channel(name, description, channelType);
        channelRepository.save(channel);
        return channel;
    }

    @Override
    public Channel findByChannel(UUID ChannelId) {
        Channel channel = channelRepository.findById(ChannelId);
        if (channel == null) {
            throw new NoSuchElementException("존재하지 않는 채널입니다.");
        }
        return channel;
    }

    @Override
    public List<Channel> findAllChannel() {
        List<Channel> channels = channelRepository.findAll();
        if (channels.isEmpty()) {
            throw new NoSuchElementException("채널이 존재하지 않습니다.");
        }
        return channels;
    }

    @Override
    public Channel updateChannel(UUID ChannelId, String name, String description, ChannelType channelType) {
        Channel channel = channelRepository.findById(ChannelId);
        if (channel == null) {
            throw new NoSuchElementException("존재 하지 않는 채널입니다.");
        }
        if (name != null && !name.isBlank()){
            channel.updateChannel(name);
        }
        if (description != null && !description.isBlank()){
            channel.updateChannelDescription(description);
        }
        if (channelType != null) {
            channel.updateIsChannelType(channelType);
        }
        channelRepository.save(channel);
        return channel;
    }

    @Override
    public void deleteChannel(UUID id) {
        Channel channel = channelRepository.findById(id);
        if (channel == null) {
            throw new NoSuchElementException("존재 하지 않는 채널 입니다.");
        }
        channelRepository.deleteById(id);
    }
}
