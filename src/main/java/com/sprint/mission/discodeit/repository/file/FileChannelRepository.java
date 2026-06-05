package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Repository
public class FileChannelRepository implements ChannelRepository {

    private Map<UUID, Channel> storage = new HashMap<>();
    private final Path filePath;

    public FileChannelRepository() {
        this.filePath = Path.of("data/channels.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (!Files.exists(filePath)) {
            this.storage = new HashMap<>();
        } else {
            this.storage = load();
        }
    }

    @Override
    public Channel save(Channel channel) {
        storage.put(channel.getId(), channel);
        saveToFile();
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        return storage.get(id);
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(storage.values());
    }

    @Override
    public void delete(UUID id) {
        storage.remove(id);
        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new java.io.FileOutputStream(filePath.toFile()))) {
            oos.writeObject(storage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<UUID, Channel> load() {
        if (!Files.exists(filePath)) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new java.io.FileInputStream(filePath.toFile()))) {
            return (Map<UUID, Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}