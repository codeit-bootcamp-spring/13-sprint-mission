package com.sprint.mission.discodeit.repository.file;
import com.sprint.mission.discodeit.entity.Channel;
import com.sprint.mission.discodeit.repository.ChannelRepository;

import java.io.*;
import java.util.*;

public class FileChannelRepository implements ChannelRepository {
    private final File file;
    private Map<UUID, Channel> data;
    public FileChannelRepository(String path) {
        this.file = new File(path);
        this.data = load();
    }

    @Override public Channel save(Channel channel) {
        data.put(channel.getId(), channel);
        saveToFile();
        return channel;
    }

    @Override public Optional<Channel> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override public List<Channel> findAll() {
        return new ArrayList<>(data.values());
    }

    @Override public void delete(UUID id) {
        data.remove(id);
        saveToFile();
    }

    private void saveToFile() {
        try (ObjectOutputStream oos =
                     new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(data);
        }

        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<UUID, Channel> load() {
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream( new FileInputStream(file))) {
            return (Map<UUID, Channel>) ois.readObject();
        }

        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
