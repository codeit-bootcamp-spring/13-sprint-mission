package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FileChannelRepository extends FileRepositoryRoot<Channel> implements ChannelRepository {

    //ctor
    public FileChannelRepository() {
        super(Path.of("data/channels.ser"));
    }

    //interface
    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void createChannel(Channel channel) {
        storage.add(channel);
        saveToBinary();
    }

    @Override
    public Optional<Channel> findChannel(Channel channel) {
        if (storage.contains(channel)){
            return Optional.of(channel);
        }
        return Optional.empty();
    }

    @Override
    public List<Channel> findAll() {
        return storage;
    }

    @Override
    public void deleteChannel(Channel channel) {
        storage.remove(channel);
        saveToBinary();
    }
}
