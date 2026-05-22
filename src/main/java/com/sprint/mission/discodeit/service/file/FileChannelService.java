package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileChannelService implements Serializable, ChannelService {

    private static final long serialVersionUID = 1L;
    private final List<Channel> storage = new ArrayList<>();

    @Override
    public void create(Channel channel) {
        if (channel == null || channel.getId() == null) {
            throw new IllegalArgumentException("채널 정보가 없습니다.");
        }
        storage.add(channel);
    }

    @Override
    public Channel read(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        for (Channel channel : storage) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }

        throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
    }

    @Override
    public List<Channel> readAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public void update(UUID id, Channel channel) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }

        if (channel == null || channel.getId() == null) {
            throw new IllegalArgumentException("채널 정보가 없습니다.");
        }

        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(id)) {
                storage.set(i, channel);
                return;
            }
        }

        throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
    }

    @Override
    public void delete(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("채널 ID는 필수입니다.");
        }
        storage.removeIf(channel -> channel.getId().equals(id));
    }

    // 직렬화
    public void saveToFile(Path channelPath) throws IOException {
        Path parent = channelPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try(ObjectOutputStream cio =
                    new ObjectOutputStream(
                            new BufferedOutputStream(
                                    Files.newOutputStream(channelPath)))){

            cio.writeObject(new ArrayList<>(storage));
        }


    }

    // 역직렬화
    public static FileChannelService loadFromFile(Path channelPath) {
        FileChannelService service = new FileChannelService();

        try(ObjectInputStream coo =
                    new ObjectInputStream(
                            new BufferedInputStream(
                                    Files.newInputStream(channelPath)))) {

            service.storage.addAll((List<Channel>) coo.readObject());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return service;
    }

}
