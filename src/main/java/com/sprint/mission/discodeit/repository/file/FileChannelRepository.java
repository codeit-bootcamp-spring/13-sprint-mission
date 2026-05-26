package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FileChannelRepository implements ChannelRepository {

    private List<Channel> storage;
    private final Path filePath;

    public FileChannelRepository() {
        this.filePath = Path.of("data/channels.ser");

        try {
            Files.createDirectories(filePath.getParent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        storage = load();
    }


    @Override
    public Channel save(Channel channel) {

        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(channel.getId())) {
                storage.remove(i);
                break;
            }
        }
        storage.add(channel);
        saveToFile();
        return channel;
    }

    @Override
    public Channel findById(UUID id) {
        for (Channel channel : storage) {
            if (channel.getId().equals(id)) {
                return channel;
            }
        }
        return null;
    }

    @Override
    public List<Channel> findAll() {
        return new ArrayList<>(storage);
    }

    @Override
    public void delete(UUID id) {
        for (int i = 0; i < storage.size(); i++) {
            if (storage.get(i).getId().equals(id)) {
                storage.remove(i);
                break;
            }
        }
        saveToFile();

    }

    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(filePath))) {
            oos.writeObject(storage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<Channel> load() {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(filePath))) {
            return (List<Channel>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
