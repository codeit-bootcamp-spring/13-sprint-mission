package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.entity.ChannelType;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {
    private final Path path;

    public FileChannelService(String path) {
        this.path = Paths.get(path);
    }

    private void saveChannelFile(List<Channel> channels) {
        Path parent = path.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            oos.writeObject(new ArrayList<>(channels));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Channel> loadChannelFile() {
        if (!Files.exists(path)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return (List<Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Channel create(ChannelType type, String channelName, String description) {
        Channel channel = new Channel(type, channelName, description);
        List<Channel> channels = loadChannelFile();

        channels.add(channel);
        saveChannelFile(channels);

        System.out.println(channelName + " 채널이 생성되었습니다!");
        return channel;
    }

    @Override
    public Channel read(UUID id) {
        List<Channel> channels = loadChannelFile();

        for (Channel channel : channels) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }
        System.out.println("채널이 존재하지 않습니다.");
        return null;
    }

    @Override
    public List<Channel> readAll() {
        return loadChannelFile();
    }

    @Override
    public void update(UUID id, ChannelType type, String channelName, String description) {
        List<Channel> channels = loadChannelFile();

        for (Channel channel : channels) {
            if (channel.getId().equals(id)) {
                channel.update(type, channelName, description);
                break;
            }
        }

        saveChannelFile(channels);
    }

    @Override
    public void delete(UUID id) {
        List<Channel> channels = loadChannelFile();

        for (Channel channel : channels) {
            if(channel.getId().equals(id)) {
                channels.remove(channel);
                break;
            }
        }

        saveChannelFile(channels);
    }
}