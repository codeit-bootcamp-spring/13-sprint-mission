package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.service.ChannelService;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelService implements ChannelService {

    // channels 디렉토리 경로
    private final Path directory;

    public FileChannelService() {
        this.directory = Paths.get(
                System.getProperty("user.dir"),
                "data",
                "channels"
        );
        init(directory);
    }

    public static void init(Path directory) {
        // 저장할 경로의 파일 초기화
        if (!Files.exists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void createChannel(Channel channel) {
        Path filePath =
                directory.resolve(channel.getId() + ".ser");

        save(filePath, channel);
    }

    @Override
    public Channel findChannel(UUID id) {
        Path filePath =
                directory.resolve("Channel " + id + ".ser");
        if (!Files.exists(filePath)) {
            return null;
        }
        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             ObjectInputStream ois = new ObjectInputStream(fis)
        ) {
            return (Channel) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Channel> findAllChannels() {
        if (Files.exists(directory)) {
            try {
                List<Channel> list = Files.list(directory)
                        .map(path -> {
                            try (
                                    FileInputStream fis = new FileInputStream(path.toFile());
                                    ObjectInputStream ois = new ObjectInputStream(fis)
                            ) {
                                Object data = ois.readObject();
                                return (Channel) data;
                            } catch (IOException | ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .toList();
                return list;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    public void updateChannel(UUID id, String name, Channel.ChannelType type, String description) {
        Channel channel = findChannel(id);
        if (channel == null) {
            return;
        }

        channel.update(name, type, description);

        Path filePath = directory.resolve("Channel " + id + ".ser");

        save(filePath, channel);
    }

    @Override
    public void deleteChannel(UUID id) {
        Path filePath = directory.resolve("Channel " + id + ".ser");

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 객체 저장
    private <T> void save(Path filePath, T data) {
        try(
                FileOutputStream fos = new FileOutputStream(filePath.toFile());
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
