package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import com.sprint.mission.discodeit.service.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileChannelService implements ChannelService {

    private final ChannelRepository repository;

    public FileChannelService(ChannelRepository repository) {
        this.repository = repository;
    }

    @Override
    public void create(Channel channel) {
        if (channel == null || channel.getId() == null) {
            throw new IllegalArgumentException("채널 정보가 없습니다.");
        }
        repository.create(channel);
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
    public void update(UUID id, Channel channel) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (channel == null || channel.getId() == null) {
            throw new IllegalArgumentException("채널 정보가 없습니다.");
        }
        if (repository.read(id) == null) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }
        repository.update(id, channel);
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