package com.sprint.mission.discodeit.service.file;

import com.sprint.mission.discodeit.entity.*;
import com.sprint.mission.discodeit.service.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileChannelService implements ChannelService {

    private static final long serialVersionUID = 1L;
    private final List<Channel> storage = new ArrayList<>();
    private final Path channelPath;

    public FileChannelService(Path channelPath) {
        this.channelPath = channelPath;
        loadFromFile();
    }

    @Override
    public void create(Channel channel) {
        if (channel == null || channel.getId() == null) {
            throw new IllegalArgumentException("채널 정보가 없습니다.");
        }

        storage.add(channel);
        saveToFile();
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
                saveToFile();
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

        boolean removed = storage.removeIf(channel -> channel.getId().equals(id));

        if (!removed) {
            throw new IllegalArgumentException("존재하지 않는 채널 ID입니다.");
        }

        saveToFile();
    }

    private void saveToFile() {
        try {
            Path parent = channelPath.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (ObjectOutputStream oos =
                         new ObjectOutputStream(
                                 new BufferedOutputStream(
                                         Files.newOutputStream(channelPath)))) {

                oos.writeObject(new ArrayList<>(storage));
            }

        } catch (IOException e) {
            throw new RuntimeException("채널 파일 저장 중 오류가 발생했습니다.", e);
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(channelPath)) {
            return;
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(
                             new BufferedInputStream(
                                     Files.newInputStream(channelPath)))) {

            storage.addAll((List<Channel>) ois.readObject());

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("채널 파일 불러오기 중 오류가 발생했습니다.", e);
        }
    }
}