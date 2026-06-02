package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
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
    public boolean existsChannelByName(String name) {
        return storage.stream()
                .anyMatch(chanel -> chanel.getName().equals(name));
    }

    @Override
    public void createChannel(Channel channel) {
        storage.add(channel);
        saveToBinary();
    }

    @Override
    public Optional<Channel> findChannelByName(String name) {
        return storage.stream()
                .filter(channel -> channel.getName().equals(name))
                .findFirst();
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
