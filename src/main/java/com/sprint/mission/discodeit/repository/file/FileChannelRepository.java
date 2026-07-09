package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileChannelRepository implements ChannelRepository {

    private Map<UUID, Channel> database;
    private final Path filePath;

    public FileChannelRepository(String fileDirectory) {
        this.filePath = Path.of(fileDirectory, "channel.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException("데이터 디렉토리 생성 실패", e);
        }

        this.database = load();
    }

    @Override
    public Channel save(Channel channel) {
        database.put(channel.getId(), channel);
        saveToFile();
        return channel;
    }

    @Override
    public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void delete(UUID id) {
        database.remove(id);
        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(Files.newOutputStream(filePath))) {

            oos.writeObject(database);

        } catch (IOException e) {
            throw new RuntimeException("파일 저장 실패", e);
        }
    }


    @SuppressWarnings("unchecked")
    private Map<UUID, Channel> load() {

        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(Files.newInputStream(filePath))) {

            return (Map<UUID, Channel>) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("파일 로드 실패", e);
        }
    }
}