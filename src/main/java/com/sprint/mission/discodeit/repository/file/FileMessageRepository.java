package com.sprint.mission.discodeit.repository.file;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.repository.MessageRepository;

import java.io.*;
import java.util.*;

public class FileMessageRepository implements MessageRepository {
    private final File file;
    private Map<UUID, Message> data;
    public FileMessageRepository(String path) {
        this.file = new File(path);
        this.data = load();
    }

    @Override public Message save(Message message) {
        data.put(message.getId(), message);
        saveToFile();
        return message;
    }

    @Override public Optional<Message> findById(UUID id) {
        return Optional.ofNullable(data.get(id));
    }

    @Override public List<Message> findAll() {
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

    private Map<UUID, Message> load() {
        if (!file.exists()) {
            return new HashMap<>();
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(file))) {
            return (Map<UUID, Message>) ois.readObject();
        }

        catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}