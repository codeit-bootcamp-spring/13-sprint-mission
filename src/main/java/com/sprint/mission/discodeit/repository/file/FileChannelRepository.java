package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.repository.*;
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.stereotype.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Repository
@ConditionalOnProperty(name = "discodeit.repository.type", havingValue = "file")
public class FileChannelRepository implements ChannelRepository {

    private final List<Channel> channels = new ArrayList<>();
    private final Path channelPath;

    public FileChannelRepository() {
        this(Paths.get("data/channels.ser"));
    }

    public FileChannelRepository(Path channelPath) {
        this.channelPath = channelPath;
        loadFromFile();
    }

    @Override
    public void create(Channel channel){
        channels.add(channel);
        saveToFile();
    }

    @Override
    public Channel find(UUID id) {
        for (Channel channel : channels) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }

        return null;
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(channels);
    }

    @Override
    public void update(UUID id, Channel channel) {
        for (int i = 0; i < channels.size(); i++) {
            if (channels.get(i).getId().equals(id)) {
                channels.set(i, channel);
                saveToFile();
                return;
            }
        }
    }

    @Override
    public void delete(UUID id) {
        channels.removeIf(channel -> channel.getId().equals(id));
        saveToFile();
    }

    public boolean exists(UUID id) {
        return channels.stream().anyMatch(channel -> channel.getId().equals(id));
    }

    private void saveToFile() {
        try {
            Path parent = channelPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(channelPath)))) {

                oos.writeObject(channels);
            }

        } catch (IOException e) {
            throw new RuntimeException("채널 파일 저장 중 오류가 발생했습니다.", e);
        }
    }


    private void loadFromFile() {
        if (!Files.exists(channelPath)) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(Files.newInputStream(channelPath)))) {

            List<Channel> loadedChannels = (List<Channel>) ois.readObject();

            channels.clear();
            channels.addAll(loadedChannels);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("채널 파일 불러오기 중 오류가 발생했습니다.", e);
        }
    }
}
