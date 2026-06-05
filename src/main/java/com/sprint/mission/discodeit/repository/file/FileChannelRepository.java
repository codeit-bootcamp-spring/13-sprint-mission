package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@ConditionalOnProperty(
        value = "discodeit.repository.type",
        havingValue = "file"
)
public class FileChannelRepository extends FileRepositoryRoot<Channel> implements ChannelRepository {

    //ctor
    public FileChannelRepository(@Value("${discodeit.repository.file-directory}") String fileDirectory) {
        super(Path.of(fileDirectory).resolve("channels.ser"));
    }

    //interface
    @Override
    public boolean existsChannelById(UUID channelId) {
        return storage.stream()
                .anyMatch(channel -> channel.getId().equals(channelId));
    }

    @Override
    public void createChannel(Channel channel) {
        storage.add(channel);

        saveToBinary();
    }

    @Override
    public Optional<Channel> findChannelById(UUID channelId) {
        return storage.stream()
                .filter(channel -> channel.getId().equals(channelId))
                .findFirst();
    }

    @Override
    public List<Channel> findAllChannelsByChannelType(ChannelType channelType) {
        return storage.stream()
                .filter(channel -> channel.getType() == channelType)
                .toList();
    }

    @Override
    public void save() {
        saveToBinary();
    }

    @Override
    public void deleteChannel(UUID channelId) {
        storage.remove(
                storage.stream()
                        .filter(channel -> channel.getId().equals(channelId))
                        .findFirst()
                        .get()
        );

        saveToBinary();
    }
}
