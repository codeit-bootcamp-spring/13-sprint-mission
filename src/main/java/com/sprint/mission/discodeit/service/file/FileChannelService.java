package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;


public class FileChannelService implements ChannelService {

    private final ChannelRepository repository;

    public FileChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public Channel create(String name, String description, ChannelType type) {
        Channel channel = new Channel(name, description, type);

        repository.create(channel);

        return channel;
    }

    @Override
    public Channel read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }
        Channel channel = repository.read(id);

        if (channel == null) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }

        return channel;
    }

    @Override
    public List<Channel> readAll() {
        return repository.readAll();
    }

    @Override
    public Channel update(UUID id, String name, String description, ChannelType type) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (!repository.exists(id)) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }

        Channel channel = repository.read(id);
        channel.update(name, description, type);

        repository.update(id, channel);

        return channel;
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }
        if (repository.read(id) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }
        repository.delete(id);
    }
}